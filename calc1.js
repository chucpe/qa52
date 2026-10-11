// ==UserScript==
// @name         e8.ffa.su — подсчёт паллет и суммы
// @namespace    https://e8.ffa.su/
// @version      2.5.5
// @description  Подсчёт паллет/суммы, фильтр по датам, настройки, автосворачивание, адаптив через JS
// @match        https://e8.ffa.su/*
// @grant        GM_xmlhttpRequest
// @grant        GM_addStyle
// @grant        unsafeWindow
// @connect      raw.githubusercontent.com
// @run-at       document-start
// ==/UserScript==

(function () {
  'use strict';

  const PRICE_URL = 'https://raw.githubusercontent.com/chucpe/qa52/master/src/price.json';
  const MSK_OFFSET_MIN = 3 * 60;
  const LS_MIN_KEY = 'e8-calc-minimized';
  const LS_SETTINGS_KEY = 'e8-calc-settings';

  const W = typeof unsafeWindow !== 'undefined' ? unsafeWindow : window;

  let priceMap = null;
  let lastResult = null;
  let lastJson = null;
  let filter = { from: null, to: null };

  let settings = {
    opacity: 0.95,
    rate: 0.48,
    tax: 13,
    autoMinimize: false,
  };

  function loadSettings() {
    try {
      const raw = localStorage.getItem(LS_SETTINGS_KEY);
      if (raw) {
        const s = JSON.parse(raw);
        if (typeof s.opacity === 'number') settings.opacity = Math.min(1, Math.max(0.05, s.opacity));
        if (typeof s.rate === 'number' && s.rate > 0) settings.rate = s.rate;
        if (typeof s.tax === 'number' && s.tax >= 0 && s.tax < 100) settings.tax = s.tax;
        if (typeof s.autoMinimize === 'boolean') settings.autoMinimize = s.autoMinimize;
      }
    } catch (e) {}
  }
  function saveSettings() {
    try { localStorage.setItem(LS_SETTINGS_KEY, JSON.stringify(settings)); } catch (e) {}
  }

  const FORMULA = (holes, pallets) =>
    round2(holes * settings.rate * (1 - settings.tax / 100) * pallets);

  const log = (...a) => console.log('%c[e8-calc]', 'color:#09f;font-weight:bold', ...a);

  function round2(n) { return Math.round((Number(n) + Number.EPSILON) * 100) / 100; }

  function sortIsoAsc(arr) {
    return arr.slice().sort((a, b) => new Date(a).getTime() - new Date(b).getTime());
  }

  function toMskString(iso) {
    if (!iso) return '—';
    const d = new Date(iso);
    if (isNaN(d)) return '—';
    const msk = new Date(d.getTime() + MSK_OFFSET_MIN * 60 * 1000);
    const p = (x) => String(x).padStart(2, '0');
    return `${p(msk.getUTCDate())}.${p(msk.getUTCMonth() + 1)}.${msk.getUTCFullYear()} ${p(msk.getUTCHours())}:${p(msk.getUTCMinutes())}`;
  }

  function palletTimeMs(pallet) {
    if (!pallet?.cancelDateTime) return null;
    const t = new Date(pallet.cancelDateTime).getTime();
    return isNaN(t) ? null : t;
  }

  function mskInputValue(kind) {
    const msk = new Date(Date.now() + MSK_OFFSET_MIN * 60 * 1000);
    const p = (x) => String(x).padStart(2, '0');
    const Y = msk.getUTCFullYear();
    const Mo = p(msk.getUTCMonth() + 1);
    const D = p(msk.getUTCDate());
    if (kind === 'start') return `${Y}-${Mo}-${D}T00:00`;
    if (kind === '08')    return `${Y}-${Mo}-${D}T08:00`;
    if (kind === '17')    return `${Y}-${Mo}-${D}T17:00`;
    return `${Y}-${Mo}-${D}T${p(msk.getUTCHours())}:${p(msk.getUTCMinutes())}`;
  }

  function mskLocalToUtcMs(s) {
    const m = String(s).match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/);
    if (!m) return null;
    const [, Y, Mo, D, H, Mi] = m;
    const utcMs = Date.UTC(+Y, +Mo - 1, +D, +H, +Mi, 0);
    return utcMs - MSK_OFFSET_MIN * 60 * 1000;
  }

  function normalize(s) {
    return String(s || '').toLowerCase().replace(/\s+/g, '').replace(/[«»"']/g, '').trim();
  }
  function baseName(s) { return normalize(s).replace(/\([^)]*\)/g, ''); }

  function loadPrice() {
    return new Promise((resolve, reject) => {
      GM_xmlhttpRequest({
        method: 'GET',
        url: PRICE_URL + '?t=' + Date.now(),
        onload: (res) => {
          try {
            const arr = JSON.parse(res.responseText);
            const map = new Map();
            for (const it of arr) {
              if (!it || !it.name) continue;
              map.set(normalize(it.name), it.holes);
              map.set(baseName(it.name), it.holes);
            }
            priceMap = map;
            log('Прайс загружен, записей:', arr.length);
            resolve(map);
          } catch (e) { reject(e); }
        },
        onerror: reject,
      });
    });
  }

  function findHoles(label) {
    if (!priceMap) return null;
    const n = normalize(label);
    if (priceMap.has(n)) return priceMap.get(n);
    const b = baseName(label);
    if (priceMap.has(b)) return priceMap.get(b);
    for (const [k, v] of priceMap) {
      if (b && k.startsWith(b) && b.length > 4) return v;
    }
    return null;
  }

  function extractItems(json) {
    if (!json) return null;
    if (Array.isArray(json.items)) return json.items;
    if (Array.isArray(json.data?.items)) return json.data.items;
    if (Array.isArray(json.result?.items)) return json.result.items;
    if (Array.isArray(json.data)) return json.data;
    if (Array.isArray(json)) return json;
    return null;
  }

  function processJobs(json) {
    lastJson = json;
    const items = extractItems(json);
    if (!items) { log('Нет items'); return; }

    const products = json?.other?.dictionary?.products
                  || json?.data?.other?.dictionary?.products
                  || [];
    const productMap = new Map(products.map(p => [p.id, p.label]));

    let totalPallets = 0, totalSum = 0, unknownPallets = 0;
    let totalPalletsAll = 0;
    const unknownLabels = new Set();
    const unknownDetails = [];
    const byOrder = new Map();
    const debugRows = [];

    const from = filter.from;
    const to   = filter.to;

    for (const order of items) {
      const orderStat = byOrder.get(order.label) || { pallets: 0, sum: 0 };
      for (const group of (order.children || [])) {
        for (const wp of (group.children || [])) {
          const pallets = Number(wp.count) || 0;
          if (!pallets) continue;

          const label = productMap.get(group.productId) || group.productId;
          const holes = findHoles(label);
          const childList = wp.children || [];

          const timesInRange = [];
          for (const c of childList) {
            const t = palletTimeMs(c);
            if (t == null) continue;
            if ((from == null || t >= from) && (to == null || t <= to)) {
              timesInRange.push(c.cancelDateTime);
            }
          }
          const sortedTimesInRange = sortIsoAsc(timesInRange);

          let palletsInRange = 0;
          if (from == null && to == null) {
            palletsInRange = pallets;
            const all = childList.map(c => c.cancelDateTime).filter(Boolean);
            timesInRange.length = 0;
            timesInRange.push(...sortIsoAsc(all));
          } else if (childList.length === 0) {
            palletsInRange = pallets;
          } else {
            palletsInRange = Math.min(sortedTimesInRange.length, pallets);
            timesInRange.length = 0;
            timesInRange.push(...sortedTimesInRange);
          }

          const firstDateIso = timesInRange[0] || null;
          totalPalletsAll += pallets;

          if (palletsInRange === 0) {
            debugRows.push({
              order: order.label, label, holes: holes ?? '—', pallets,
              palletsInRange: 0, firstDate: toMskString(firstDateIso),
              sum: 0, note: 'вне диапазона', times: [],
              firstDateMs: firstDateIso ? new Date(firstDateIso).getTime() : Infinity,
            });
            const allTimes = sortIsoAsc(childList.map(c => c.cancelDateTime).filter(Boolean));
            unknownDetails.push({ order: order.label, label, times: allTimes, reason: 'вне диапазона', pallets });
            continue;
          }

          if (holes == null) {
            unknownPallets += palletsInRange;
            unknownLabels.add(label);
            orderStat.pallets += palletsInRange;
            debugRows.push({
              order: order.label, label, holes: '—', pallets,
              palletsInRange, firstDate: toMskString(firstDateIso),
              sum: 0, note: 'нет в прайсе', times: timesInRange,
              firstDateMs: firstDateIso ? new Date(firstDateIso).getTime() : Infinity,
            });
            unknownDetails.push({ order: order.label, label, times: timesInRange, reason: 'нет в прайсе', pallets: palletsInRange });
            continue;
          }

          const sum = FORMULA(holes, palletsInRange);
          totalPallets += palletsInRange;
          totalSum = round2(totalSum + sum);
          orderStat.pallets += palletsInRange;
          orderStat.sum = round2(orderStat.sum + sum);

          debugRows.push({
            order: order.label, label, holes, pallets,
            palletsInRange, firstDate: toMskString(firstDateIso),
            sum, times: timesInRange,
            firstDateMs: firstDateIso ? new Date(firstDateIso).getTime() : Infinity,
          });
        }
      }
      byOrder.set(order.label, orderStat);
    }

    lastResult = {
      totalPallets, totalSum: round2(totalSum), unknownPallets,
      unknownLabels: [...unknownLabels], byOrder, totalPalletsAll,
      debugRows, unknownDetails,
    };
    log('Результат:', lastResult);
    console.table(debugRows);
    renderWindow();
  }

  function patchFetch(target) {
    if (!target || !target.fetch || target.__e8patched) return;
    target.__e8patched = true;
    const orig = target.fetch;
    target.fetch = async function (...args) {
      const url = typeof args[0] === 'string' ? args[0] : args[0]?.url || '';
      const res = await orig.apply(this, args);
      try {
        if (url.includes('/jobs') && url.includes('statusId=2')) {
          res.clone().json().then(processJobs).catch(() => {});
        }
      } catch (e) {}
      return res;
    };
  }
  patchFetch(W);
  if (W !== window) patchFetch(window);

  const XHR = W.XMLHttpRequest || window.XMLHttpRequest;
  if (XHR && !XHR.prototype.__e8patched) {
    XHR.prototype.__e8patched = true;
    const origOpen = XHR.prototype.open;
    const origSend = XHR.prototype.send;
    XHR.prototype.open = function (method, url, ...rest) {
      this.__e8url = url;
      return origOpen.call(this, method, url, ...rest);
    };
    XHR.prototype.send = function (...args) {
      this.addEventListener('load', function () {
        try {
          const url = this.__e8url || '';
          if (url.includes('/jobs') && url.includes('statusId=2')) {
            processJobs(JSON.parse(this.responseText));
          }
        } catch (e) {}
      });
      return origSend.apply(this, args);
    };
  }

  GM_addStyle(`
    #e8-calc-box{
      position:fixed;right:20px;bottom:20px;left:auto;z-index:999999;
      width:360px;max-width:calc(100vw - 24px);box-sizing:border-box;
      background:#1e1e2e;color:#eee;border:1px solid #444;border-radius:10px;
      font:13px/1.4 system-ui,sans-serif;box-shadow:0 8px 24px rgba(0,0,0,.5);
      padding:12px 14px;
    }
    #e8-calc-box.hidden{display:none}
    #e8-calc-box .row{display:flex;justify-content:space-between;margin:4px 0}
    #e8-calc-box .row.clickable{cursor:pointer;border-radius:4px;padding:2px 4px;margin-left:-4px;margin-right:-4px}
    #e8-calc-box .row.clickable:hover{background:#2a2a3e}
    #e8-calc-box .val{font-weight:700}
    #e8-calc-box .sum{color:#7CFC9B;font-size:15px}
    #e8-calc-box .unk{color:#ffb86c}
    #e8-calc-box .unk-red{color:#ff5555;font-weight:700}
    #e8-calc-box button{margin-top:8px;width:100%;cursor:pointer;background:#3b82f6;color:#fff;border:0;border-radius:6px;padding:6px 8px;font-weight:600}
    #e8-calc-box button:hover{background:#2563eb}
    #e8-calc-box button.secondary{background:#334155}
    #e8-calc-box button.secondary:hover{background:#475569}
    #e8-calc-box .ord{font-size:11px;opacity:.85;border-top:1px solid #333;margin-top:6px;padding-top:6px;max-height:240px;overflow:auto}
    #e8-calc-box .ord-head{display:flex;justify-content:space-between;padding:3px 4px;border-radius:4px;cursor:pointer}
    #e8-calc-box .ord-head:hover{background:#2a2a3e}
    #e8-calc-box .ord-head .arr{opacity:.5;margin-right:4px;transition:transform .15s;display:inline-block}
    #e8-calc-box .ord-head.open .arr{transform:rotate(90deg)}
    #e8-calc-box .ord-detail{display:none;padding:2px 4px 6px 18px;background:#161622;border-left:2px solid #334155;margin:2px 0 4px 4px;border-radius:0 4px 4px 0}
    #e8-calc-box .ord-detail.open{display:block}
    #e8-calc-box .pal{display:flex;justify-content:space-between;gap:8px;padding:2px 0;font-size:11px}
    #e8-calc-box .pal .t{opacity:.7;white-space:nowrap}
    #e8-calc-box .pal .n{flex:1;text-align:right;opacity:.95}
    #e8-calc-box .pal-head{font-size:11px;padding:2px 0;text-align:left}
    #e8-calc-box .pal-sep{border-top:1px dashed #333;margin:4px 0}
    #e8-calc-box .unk-reason{font-size:10px;color:#ffb86c;opacity:.9}

    #e8-calc-dates,#e8-calc-settings{display:none;margin-top:8px;padding-top:8px;border-top:1px dashed #444}
    #e8-calc-dates label,#e8-calc-settings label{display:block;font-size:11px;opacity:.8;margin:4px 0 2px}
    #e8-calc-dates input,#e8-calc-settings input{width:100%;box-sizing:border-box;background:#0f172a;color:#eee;border:1px solid #334155;border-radius:5px;padding:4px 6px;font:12px system-ui;color-scheme:dark}
    #e8-calc-dates .btns,#e8-calc-settings .btns{display:flex;gap:6px;margin-top:8px}
    #e8-calc-dates .btns button,#e8-calc-settings .btns button{margin-top:0;flex:1}
    #e8-calc-settings input[type=range]{padding:0;background:transparent;border:0}
    #e8-calc-settings input[type=checkbox]{width:auto;margin:0}
    #e8-calc-settings .hint{font-size:10px;opacity:.6;margin-top:2px}
    #e8-calc-settings .chk{display:flex;align-items:center;gap:6px;margin-top:8px;font-size:11px;opacity:.9}

    #e8-calc-mini{
      position:fixed;right:20px;bottom:20px;z-index:999999;
      width:46px;height:46px;border-radius:50%;
      background:#3b82f6;color:#fff;border:0;
      font:700 20px/1 system-ui,sans-serif;cursor:pointer;
      box-shadow:0 4px 14px rgba(0,0,0,.5);
      display:flex;align-items:center;justify-content:center;
      transition:background .15s, transform .1s;
    }
    #e8-calc-mini:hover{background:#2563eb;transform:scale(1.05)}
    #e8-calc-mini.hidden{display:none}
  `);

  function applyAdaptiveLayout() {
    const box = document.getElementById('e8-calc-box');
    if (!box) return;
    const narrow = window.innerWidth < 460;
    if (narrow) {
      box.style.left = '12px';
      box.style.right = '12px';
      box.style.width = 'auto';
      box.style.maxWidth = 'none';
      box.style.bottom = '12px';
    } else {
      box.style.left = 'auto';
      box.style.right = '20px';
      box.style.width = '360px';
      box.style.maxWidth = 'calc(100vw - 24px)';
      box.style.bottom = '20px';
    }
  }

  window.addEventListener('resize', applyAdaptiveLayout);
  window.addEventListener('orientationchange', applyAdaptiveLayout);

  function ensureBox() {
    let box = document.getElementById('e8-calc-box');
    if (box) return box;
    box = document.createElement('div');
    box.id = 'e8-calc-box';

    const fromVal = mskInputValue('08');
    const toVal   = mskInputValue('17');

    box.innerHTML = `
      <div id="e8-calc-body">Ожидание данных…</div>

      <div id="e8-calc-dates">
        <label>От:</label>
        <input type="datetime-local" id="e8-date-from" step="60" value="${fromVal}">
        <label>До:</label>
        <input type="datetime-local" id="e8-date-to" step="60" value="${toVal}">
        <div class="btns">
          <button id="e8-date-apply">Применить</button>
          <button id="e8-date-reset" class="secondary">Сбросить</button>
        </div>
      </div>

      <div id="e8-calc-settings">
        <label>Прозрачность: <span id="e8-op-val">${Math.round(settings.opacity * 100)}%</span></label>
        <input type="range" id="e8-op-range" min="5" max="100" step="1" value="${Math.round(settings.opacity * 100)}">

        <label>Ставка калькулятора:</label>
        <input type="number" id="e8-rate-input" step="0.01" min="0.01" value="${settings.rate}">
        <div class="hint">Сумма = отверстия × ставка × (1 − НДФЛ) × паллеты</div>

        <label>НДФЛ, %:</label>
        <input type="number" id="e8-tax-input" step="1" min="0" max="99" value="${settings.tax}">

        <label class="chk">
          <input type="checkbox" id="e8-automin" ${settings.autoMinimize ? 'checked' : ''}>
          Автоматически сворачивать после загрузки
        </label>

        <div class="btns">
          <button id="e8-settings-apply">Применить</button>
          <button id="e8-settings-reset" class="secondary">Сбросить</button>
        </div>
      </div>

      <button id="e8-calc-toggle">Считать по датам</button>
      <button id="e8-calc-recalc" class="secondary">Пересчитать</button>
      <button id="e8-calc-settings-toggle" class="secondary">Настройка</button>
      <button id="e8-calc-minimize" class="secondary">Свернуть</button>
    `;
    document.body.appendChild(box);

    let mini = document.getElementById('e8-calc-mini');
    if (!mini) {
      mini = document.createElement('button');
      mini.id = 'e8-calc-mini';
      mini.title = 'Развернуть подсчёт';
      mini.textContent = 'Σ';
      document.body.appendChild(mini);
      mini.addEventListener('click', () => setMinimized(false));
    }

    const datesPanel = box.querySelector('#e8-calc-dates');
    const settingsPanel = box.querySelector('#e8-calc-settings');

    box.querySelector('#e8-calc-toggle').addEventListener('click', () => {
      const visible = datesPanel.style.display === 'block';
      datesPanel.style.display = visible ? 'none' : 'block';
      settingsPanel.style.display = 'none';
    });

    box.querySelector('#e8-date-apply').addEventListener('click', () => {
      const fromStr = box.querySelector('#e8-date-from').value;
      const toStr   = box.querySelector('#e8-date-to').value;
      filter.from = fromStr ? mskLocalToUtcMs(fromStr) : null;
      filter.to   = toStr   ? mskLocalToUtcMs(toStr)   : null;
      log('Фильтр:', { from: filter.from && new Date(filter.from), to: filter.to && new Date(filter.to) });
      if (lastJson) processJobs(lastJson);
    });

    box.querySelector('#e8-date-reset').addEventListener('click', () => {
      box.querySelector('#e8-date-from').value = mskInputValue('08');
      box.querySelector('#e8-date-to').value   = mskInputValue('17');
      filter.from = null; filter.to = null;
      if (lastJson) processJobs(lastJson);
    });

    box.querySelector('#e8-calc-settings-toggle').addEventListener('click', () => {
      const visible = settingsPanel.style.display === 'block';
      settingsPanel.style.display = visible ? 'none' : 'block';
      datesPanel.style.display = 'none';
    });

    const opRange = box.querySelector('#e8-op-range');
    const opVal   = box.querySelector('#e8-op-val');
    const applyOpacity = () => { box.style.opacity = settings.opacity; };

    opRange.addEventListener('input', () => {
      settings.opacity = Math.min(1, Math.max(0.05, Number(opRange.value) / 100));
      opVal.textContent = Math.round(settings.opacity * 100) + '%';
      applyOpacity();
      saveSettings();
    });

    box.querySelector('#e8-automin').addEventListener('change', (e) => {
      settings.autoMinimize = e.target.checked;
      saveSettings();
      log('Автосворачивание:', settings.autoMinimize);
    });

    box.querySelector('#e8-settings-apply').addEventListener('click', () => {
      const rate = Number(box.querySelector('#e8-rate-input').value);
      const tax  = Number(box.querySelector('#e8-tax-input').value);
      if (Number.isFinite(rate) && rate > 0) settings.rate = rate;
      if (Number.isFinite(tax)  && tax >= 0 && tax < 100) settings.tax = tax;
      settings.autoMinimize = box.querySelector('#e8-automin').checked;
      saveSettings();
      log('Настройки применены:', settings);
      if (lastJson) processJobs(lastJson);
    });

    box.querySelector('#e8-settings-reset').addEventListener('click', () => {
      settings = { opacity: 0.95, rate: 0.48, tax: 13, autoMinimize: settings.autoMinimize };
      box.querySelector('#e8-op-range').value = 95;
      box.querySelector('#e8-op-val').textContent = '95%';
      box.querySelector('#e8-rate-input').value = 0.48;
      box.querySelector('#e8-tax-input').value  = 13;
      box.querySelector('#e8-automin').checked  = settings.autoMinimize;
      applyOpacity();
      saveSettings();
      if (lastJson) processJobs(lastJson);
    });

    box.querySelector('#e8-calc-recalc').addEventListener('click', () => {
      if (lastJson) processJobs(lastJson);
      else box.querySelector('#e8-calc-body').textContent = 'Данных ещё нет — обновите страницу';
    });

    box.querySelector('#e8-calc-minimize').addEventListener('click', () => setMinimized(true));

    box.style.opacity = settings.opacity;

    if (settings.autoMinimize) {
      setMinimized(true);
    } else {
      const savedMin = localStorage.getItem(LS_MIN_KEY) === '1';
      setMinimized(savedMin);
    }

    applyAdaptiveLayout();
    return box;
  }

  function setMinimized(min) {
    const box = document.getElementById('e8-calc-box');
    const mini = document.getElementById('e8-calc-mini');
    if (!box || !mini) return;
    if (min) {
      box.classList.add('hidden');
      mini.classList.remove('hidden');
      try { localStorage.setItem(LS_MIN_KEY, '1'); } catch (e) {}
    } else {
      box.classList.remove('hidden');
      mini.classList.add('hidden');
      try { localStorage.setItem(LS_MIN_KEY, '0'); } catch (e) {}
    }
  }

  function renderWindow() {
    const box = ensureBox();
    const body = box.querySelector('#e8-calc-body');
    if (!lastResult) { body.textContent = 'Ожидание данных…'; return; }

    const {
      totalPallets, totalSum, unknownPallets, unknownLabels,
      byOrder, totalPalletsAll, debugRows, unknownDetails,
    } = lastResult;
    const money = (n) => round2(n).toLocaleString('ru-RU', {
      minimumFractionDigits: 2, maximumFractionDigits: 2,
    }) + ' ₽';

    const rowsByOrder = new Map();
    for (const r of (debugRows || [])) {
      if (!rowsByOrder.has(r.order)) rowsByOrder.set(r.order, []);
      rowsByOrder.get(r.order).push(r);
    }

    let ordersHtml = '';
    if (byOrder) {
      for (const [label, st] of byOrder) {
        let rows = rowsByOrder.get(label) || [];
        rows = rows.slice().sort((a, b) => (a.firstDateMs ?? Infinity) - (b.firstDateMs ?? Infinity));

        const key = 'ord-' + Math.random().toString(36).slice(2, 8);

        let palHtml = '';
        if (rows.length === 0) {
          palHtml = `<div class="pal-head">Нет данных</div>`;
        } else {
          rows.forEach((r, idx) => {
            if (idx > 0) palHtml += `<div class="pal-sep"></div>`;
            const times = (r.times && r.times.length) ? sortIsoAsc(r.times) : [];

            palHtml += `<div class="pal-head"><b>${escapeHtml(r.label)}</b> × ${r.palletsInRange}${r.holes && r.holes !== '—' ? ` (${r.holes} отв.)` : ''} = ${money(r.sum)}</div>`;
            if (times.length === 0) {
              palHtml += `<div class="pal"><span class="t">время: —</span></div>`;
            } else {
              for (const iso of times) {
                palHtml += `<div class="pal"><span class="t">${toMskString(iso)}</span><span class="n">${escapeHtml(r.label)}</span></div>`;
              }
            }
          });
        }

        ordersHtml += `
          <div class="ord-head" data-target="${key}">
            <span><span class="arr">▶</span>${escapeHtml(label)}</span>
            <span>${st.pallets} / ${money(st.sum)}</span>
          </div>
          <div class="ord-detail" id="${key}">${palHtml}</div>
        `;
      }
    }

    let unknownHtml = '';
    if (unknownPallets > 0) {
      const key = 'unk-' + Math.random().toString(36).slice(2, 8);

      const byLabel = new Map();
      for (const d of (unknownDetails || [])) {
        if (!byLabel.has(d.label)) byLabel.set(d.label, { times: [], reasons: new Set(), pallets: 0, orders: new Set() });
        const g = byLabel.get(d.label);
        g.times.push(...(d.times || []));
        g.reasons.add(d.reason);
        g.pallets += d.pallets || 0;
        g.orders.add(d.order);
      }

      const groups = [...byLabel.entries()].map(([label, g]) => {
        g.times = sortIsoAsc(g.times);
        const firstMs = g.times.length ? new Date(g.times[0]).getTime() : Infinity;
        return { label, g, firstMs };
      }).sort((a, b) => a.firstMs - b.firstMs);

      let detailHtml = '';
      groups.forEach((entry, idx) => {
        const { label,
