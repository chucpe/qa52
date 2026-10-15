// ==UserScript==
// @name         Калькулейшин
// @namespace    Калькулейшин
// @version      3.0
// @description  Подсчёт паллет/суммы + реальное время + фильтр по датам + настройки + фиолетовые заявки + цены + статистика
// @match        https://e8.ffa.su/*
// @grant        GM_xmlhttpRequest
// @grant        GM_addStyle
// @grant        unsafeWindow
// @connect      raw.githubusercontent.com
// @connect      e8.ffa.su
// @connect      backoffice-e8.ffa.su
// @run-at       document-start
// @downloadURL https://raw.githubusercontent.com/chucpe/qa52/master/calc.user.js
// @updateURL   https://raw.githubusercontent.com/chucpe/qa52/master/calc.user.js
// ==/UserScript==

// ==================================================================
// ОБЩЕЕ ХРАНИЛИЩЕ — токен Bearer
// ==================================================================
(function() {
    'use strict';
    const W = (typeof unsafeWindow !== 'undefined') ? unsafeWindow : window;

    function loadTokenFromStorage() {
        try {
            const t = localStorage.getItem('auth_token');
            if (t && /^eyJ/.test(t)) {
                W.__e8Token = /^Bearer\s+/i.test(t) ? t : ('Bearer ' + t);
                try { window.__e8Token = W.__e8Token; } catch (e) {}
                return W.__e8Token;
            }
        } catch (e) {}
        return null;
    }

    W.__e8Token = loadTokenFromStorage() || W.__e8Token || null;
    try { window.__e8Token = W.__e8Token; } catch (e) {}

    setInterval(loadTokenFromStorage, 2000);

    function patchFetch(target) {
        if (!target || !target.fetch || target.__e8TokenPatched) return;
        target.__e8TokenPatched = true;
        const orig = target.fetch;
        target.fetch = async function (...args) {
            try {
                const opts = typeof args[1] === 'object' ? args[1] : {};
                const headers = opts?.headers || (typeof args[0] === 'object' ? args[0]?.headers : null);
                if (headers) {
                    let auth = null;
                    if (headers instanceof Headers) {
                        auth = headers.get('Authorization') || headers.get('authorization');
                    } else if (typeof headers === 'object') {
                        for (const k of Object.keys(headers)) {
                            if (/^authorization$/i.test(k)) { auth = headers[k]; break; }
                        }
                    }
                    if (auth && /^Bearer\s+/i.test(auth)) {
                        W.__e8Token = auth;
                        try { window.__e8Token = auth; } catch (e) {}
                    }
                }
            } catch (e) {}
            return orig.apply(this, args);
        };
    }
    try { patchFetch(W); } catch (e) {}
    try { if (W !== window) patchFetch(window); } catch (e) {}
    try { patchFetch(globalThis); } catch (e) {}
})();


// ==================================================================
// МОДУЛЬ СТАТИСТИКИ
// ==================================================================
(function() {
    'use strict';

    const W = (typeof unsafeWindow !== 'undefined') ? unsafeWindow : window;

    const STATS_KEY = 'e8_stats_by_product';
    const SEEN_KEY  = 'e8_seen_pallets';

    let stats = {};
    let seenPallets = [];

    function loadStats() {
        try {
            const raw = localStorage.getItem(STATS_KEY);
            if (raw) {
                const s = JSON.parse(raw);
                if (s && typeof s === 'object') stats = s;
            }
        } catch (e) {}
    }
    function saveStats() {
        try { localStorage.setItem(STATS_KEY, JSON.stringify(stats)); } catch (e) {}
    }

    function loadSeen() {
        try {
            const raw = localStorage.getItem(SEEN_KEY);
            if (raw) {
                const arr = JSON.parse(raw);
                if (Array.isArray(arr)) seenPallets = arr;
            }
        } catch (e) {}
    }
    function saveSeen() {
        try {
            // Не даём разростись больше 20000 записей
            if (seenPallets.length > 20000) seenPallets = seenPallets.slice(-20000);
            localStorage.setItem(SEEN_KEY, JSON.stringify(seenPallets));
        } catch (e) {}
    }
    function isSeen(id) {
        if (!id) return false;
        return seenPallets.indexOf(id) !== -1;
    }
    function markSeen(id) {
        if (!id) return;
        if (seenPallets.indexOf(id) === -1) seenPallets.push(id);
    }

    // Базовая статистика: защищена от двойного счёта по palletId
    function accumulateBase(label, holes, earn, palletId) {
        if (!label) return;

        // Если palletId передан и уже видели — пропускаем
        if (palletId) {
            if (isSeen(palletId)) return;
            markSeen(palletId);
            saveSeen();
        }

        if (!stats[label]) stats[label] = { holes: 0, earn: 0, count: 0, hours: 0 };
        const s = stats[label];
        if (typeof s.hours !== 'number') s.hours = 0;
        s.holes += Number(holes) || 0;
        s.earn  += Number(earn) || 0;
        s.count += 1;
        saveStats();
    }

    function addHours(label, hours) {
        if (!label || !hours || hours <= 0) return;
        if (!stats[label]) stats[label] = { holes: 0, earn: 0, count: 0, hours: 0 };
        if (typeof stats[label].hours !== 'number') stats[label].hours = 0;
        stats[label].hours += Number(hours) || 0;
        saveStats();
        console.log('[time] addHours:', label, hours.toFixed(2));
    }

    W.__e8Stats = { accumulate: accumulateBase, addHours };
    try { window.__e8Stats = W.__e8Stats; } catch (e) {}

    let progress = { active: false, done: 0, total: 0 };

    function setProgress(active, done, total) {
        progress.active = active;
        progress.done = done;
        progress.total = total;
        renderStats();
    }

    function formatHours(h) {
        if (!h || h <= 0) return '0м';
        const totalMinutes = Math.round(h * 60);
        const hours = Math.floor(totalMinutes / 60);
        const minutes = totalMinutes % 60;
        if (hours === 0) return `${minutes}м`;
        if (minutes === 0) return `${hours}ч`;
        return `${hours}ч ${minutes}м`;
    }

    function buildStatsHTML() {
        const items = Object.entries(stats);

        let totalEarn = 0, totalCount = 0, totalHours = 0;
        for (const [, s] of items) {
            totalEarn  += s.earn || 0;
            totalCount += s.count || 0;
            totalHours += s.hours || 0;
        }

        const top = items
            .map(([label, s]) => ({
                label,
                holes: s.holes || 0,
                earn: s.earn || 0,
                count: s.count || 0,
                hours: s.hours || 0,
            }))
            .sort((a, b) => b.holes - a.holes)
            .slice(0, 10);

        const money = (n) => (Math.round(n * 100) / 100).toLocaleString('ru-RU', {
            minimumFractionDigits: 2, maximumFractionDigits: 2
        }) + ' ₽';

        let html = `
            <div style="padding-top:6px;border-top:1px dashed #444;margin-top:6px;">
                <div style="display:flex;justify-content:space-between;color:#7CFC9B;font-weight:700;">
    <span>Заработок:</span>
    <span>${money(totalEarn)}</span>
</div>
<div style="display:flex;justify-content:space-between;color:#fbbf24;font-weight:700;margin-top:2px;">
    <span>Время:</span>
    <span>${formatHours(totalHours)}</span>
</div>
<div style="display:flex;justify-content:space-between;color:#a78bfa;font-weight:700;margin-top:2px;">
    <span>Паллет:</span>
    <span>${totalCount}</span>
</div>
                ${progress.active ? `
                <div style="margin-top:6px;font-size:11px;color:#60a5fa;">
                    Загружаем время: ${progress.done} / ${progress.total}…
                </div>` : ''}
                <div style="margin-top:8px;font-size:11px;opacity:.7;">Топ-10 позиций (по отверстиям):</div>
            </div>
        `;

        if (top.length === 0) {
            html += `<div style="font-size:11px;opacity:.6;margin-top:4px;">Пока нет данных</div>`;
        } else {
            top.forEach((t, i) => {
                const idx = i + 1;
                html += `
                    <div style="
                        display:flex;
                        justify-content:space-between;
                        gap:8px;
                        padding:3px 0;
                        font-size:11px;
                        border-top:1px solid #2a2a3e;
                        margin-top:2px;
                    ">
                        <span style="flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">
                            <b>${idx}.</b> ${t.label}
                        </span>
                        <span style="text-align:right;white-space:nowrap;">
    <span style="color:#60a5fa;">${t.holes} отв</span>
    · <span style="color:#fbbf24;">${formatHours(t.hours)}</span>
    · <span style="color:#7CFC9B;">${money(t.earn)}</span>
</span>
                    </div>
                `;
            });
        }

        html += `
            <button id="e8-stats-clear" style="
                margin-top:10px;
                width:100%;
                cursor:pointer;
                background:#ef4444;
                color:#fff;
                border:0;
                border-radius:5px;
                padding:4px 8px;
                font:600 11px system-ui,sans-serif;
            ">Очистить статистику</button>
        `;

        return html;
    }

        function ensureStatsPanel() {
        let panel = document.getElementById('e8-stats-panel');
        if (panel) return panel;

        panel = document.createElement('div');
        panel.id = 'e8-stats-panel';
        panel.style.cssText = `
            display:none;
            margin-top:6px;
            background:#161622;
            border-left:2px solid #334155;
            border-radius:0 4px 4px 0;
            padding:6px 8px;
            font-size:12px;
            max-height:400px;
            overflow-y:auto;
            position:relative;
            z-index:10;
            width:100%;
            box-sizing:border-box;
        `;

        // Вставляем панель ВНУТРЬ #e8-calc-box, перед кнопкой "Свернуть"
        const box = document.getElementById('e8-calc-box');
        const minimizeBtn = document.getElementById('e8-calc-minimize');

        if (box && minimizeBtn) {
            box.insertBefore(panel, minimizeBtn);
        } else if (box) {
            box.appendChild(panel);
        } else {
            document.body.appendChild(panel);
        }

        return panel;
    }

    function renderStats() {
        let panel = document.getElementById('e8-stats-panel');
        if (!panel) panel = ensureStatsPanel();
        if (!panel) return;
        panel.innerHTML = buildStatsHTML();

        const clearBtn = panel.querySelector('#e8-stats-clear');
        if (clearBtn) {
            let clearArmed = false;
            clearBtn.addEventListener('click', () => {
                if (!clearArmed) {
                    clearArmed = true;
                    clearBtn.textContent = 'Нажмите ещё раз для очистки';
                    clearBtn.style.background = '#b91c1c';
                    setTimeout(() => {
                        clearArmed = false;
                        clearBtn.textContent = 'Очистить статистику';
                        clearBtn.style.background = '#ef4444';
                    }, 3000);
                    return;
                }
                stats = {};
                seenPallets = [];
                saveStats();
                saveSeen();
                try { localStorage.removeItem('e8_pallet_times'); } catch (e) {}
                panel.innerHTML = buildStatsHTML();
            });
        }
    }

            function attachToButton() {
        const btn = document.getElementById('e8-calc-stats-toggle');
        if (!btn || btn.dataset.e8StatsBound === '1') return;
        btn.dataset.e8StatsBound = '1';

        btn.addEventListener('click', () => {
            const panel = ensureStatsPanel();
            const orders = document.getElementById('e8-calc-orders');
            if (!panel) return;

            const statsVisible = panel.style.display === 'block';

            if (statsVisible) {
                panel.style.display = 'none';
                if (orders) orders.style.display = '';
                btn.textContent = 'Статистика';
                        } else {
                renderStats();
                panel.style.display = 'block';
                if (orders) orders.style.display = 'none';
                btn.textContent = 'Статистика ✓';

                // Скрываем онлайн и его галочку
                const onlinePanel = document.getElementById('e8-online-panel');
                if (onlinePanel) onlinePanel.style.display = 'none';
                const onlineBtn = document.getElementById('e8-calc-online-toggle');
                if (onlineBtn) onlineBtn.textContent = 'Онлайн';
            }
        });
    }

    loadStats();
    loadSeen();
    attachToButton();
    setInterval(attachToButton, 1000);

    W.__e8StatsUI = { renderStats, setProgress };
    try { window.__e8StatsUI = W.__e8StatsUI; } catch (e) {}
})();


// ==================================================================
// МОДУЛЬ СБОРА ВРЕМЕНИ (через /jobs/{id}/info)
// ==================================================================
(function() {
    'use strict';

    const W = (typeof unsafeWindow !== 'undefined') ? unsafeWindow : window;

    const TIMES_KEY = 'e8_pallet_times';
    // Собираем информацию о времени потраченного на паллеты
    const API_BASE = 'https://backoffice-e8.ffa.su';
    const CONCURRENCY = 5;

    let timesCache = {};

    function loadTimes() {
        try {
            const raw = localStorage.getItem(TIMES_KEY);
            if (raw) {
                const t = JSON.parse(raw);
                if (t && typeof t === 'object') timesCache = t;
            }
        } catch (e) {}
    }
    function saveTimes() {
        try { localStorage.setItem(TIMES_KEY, JSON.stringify(timesCache)); } catch (e) {}
    }

    function collectPallets(json) {
        const products = json?.other?.dictionary?.products
                      || json?.data?.other?.dictionary?.products
                      || [];
        const productMap = new Map(products.map(p => [p.id, p.label]));

        const result = [];
        const items = json?.items || json?.data?.items;
        if (!Array.isArray(items)) {
            console.log('[time] collectPallets: items НЕ массив', items);
            return result;
        }

        for (const order of items) {
            for (const group of (order.children || [])) {
                const productId = group.productId;
                const label = productMap.get(productId) || productId || '—';

                for (const wp of (group.children || [])) {
                    for (const pallet of (wp.children || [])) {
                        const palletId = pallet?.id;
                        if (!palletId) continue;
                        result.push({
                            palletId,
                            productId,
                            label,
                            cancelDateTime: pallet?.cancelDateTime || null,
                        });
                    }
                }
            }
        }
        return result;
    }

    function parseInfoResponse(text) {
        try {
            const j = JSON.parse(text);
            const start = j?.startDateTime || j?.data?.startDateTime;
            const end = j?.cancelDateTime || j?.data?.cancelDateTime;
            if (!start || !end) return null;
            const t1 = new Date(start).getTime();
            const t2 = new Date(end).getTime();
            if (isNaN(t1) || isNaN(t2) || t2 <= t1) return null;
            return (t2 - t1) / (1000 * 60 * 60);
        } catch (e) {
            return null;
        }
    }

    function fetchPalletHours(palletId) {
        return new Promise((resolve) => {
            let token = W.__e8Token || (typeof window !== 'undefined' ? window.__e8Token : null);
            if (!token) {
                try {
                    const t = localStorage.getItem('auth_token');
                    if (t && /^eyJ/.test(t)) {
                        token = /^Bearer\s+/i.test(t) ? t : ('Bearer ' + t);
                        W.__e8Token = token;
                        try { window.__e8Token = token; } catch (e) {}
                    }
                } catch (e) {}
            }
            if (!token) {
                console.log('[time] нет токена');
                resolve({ ok: false, reason: 'no-token' });
                return;
            }

            // Путь через e8.ffa.su + ?back=
            const url = `${API_BASE}/api/v1/admin/projects/1/jobs/${palletId}`;
            GM_xmlhttpRequest({
                method: 'GET',
                url,
                headers: {
                    'Accept': 'application/json',
                    'Authorization': token,
                },
                onload: (res) => {
                    if (res.status !== 200) {
                        console.log('[time] status', res.status, 'для', palletId);
                        resolve({ ok: false, reason: 'status-' + res.status });
                        return;
                    }
                    const hours = parseInfoResponse(res.responseText);
                    if (hours === null) {
                        console.log('[time] parse fail для', palletId, res.responseText.slice(0, 200));
                        resolve({ ok: false, reason: 'parse' });
                        return;
                    }
                    resolve({ ok: true, hours });
                },
                onerror: () => { console.log('[time] network err'); resolve({ ok: false, reason: 'network' }); },
                ontimeout: () => { console.log('[time] timeout'); resolve({ ok: false, reason: 'timeout' }); },
                timeout: 15000,
            });
        });
    }

    let processing = false;

    async function processPallets(json) {
        if (processing) return;
        processing = true;

        loadTimes();

        const all = collectPallets(json);
        if (all.length === 0) { processing = false; return; }

        const newPallets = all.filter(p => !timesCache[p.palletId]);

        const total = newPallets.length;
        let done = 0;

        if (W.__e8StatsUI?.setProgress) {
            W.__e8StatsUI.setProgress(true, 0, total);
        }

        const queue = newPallets.slice();
        const workers = [];
        for (let i = 0; i < Math.min(CONCURRENCY, queue.length); i++) {
            workers.push(worker(queue, () => {
                done++;
                if (W.__e8StatsUI?.setProgress) {
                    W.__e8StatsUI.setProgress(true, done, total);
                }
            }));
        }
        await Promise.all(workers);

        if (W.__e8StatsUI?.setProgress) {
            W.__e8StatsUI.setProgress(false, 0, 0);
        }
        if (W.__e8StatsUI?.renderStats) {
            W.__e8StatsUI.renderStats();
        }

        processing = false;
    }

    async function worker(queue, onDone) {
        while (queue.length) {
            const item = queue.shift();
            if (!item) break;

            const res = await fetchPalletHours(item.palletId);
            if (res.ok && res.hours > 0) {
                timesCache[item.palletId] = {
                    hours: res.hours,
                    label: item.label,
                    date: item.cancelDateTime || new Date().toISOString(),
                };
                saveTimes();

                if (W.__e8Stats && W.__e8Stats.addHours) {
                    W.__e8Stats.addHours(item.label, res.hours);
                }
            }
            onDone();
        }
    }

    function patchFetch(target) {
        if (!target || !target.fetch || target.__e8TimePatched) return;
        target.__e8TimePatched = true;
        const orig = target.fetch;
        target.fetch = async function (...args) {
            const url = typeof args[0] === 'string' ? args[0] : args[0]?.url || '';
            const res = await orig.apply(this, args);
            try {
                if (url.includes('/jobs') && url.includes('statusId=2')) {
                    res.clone().json().then(processPallets).catch((e) => console.log('[time] json err:', e));
                }
            } catch (e) {}
            return res;
        };
    }
    patchFetch(W);
    if (W !== window) patchFetch(window);

    const XHR = W.XMLHttpRequest || window.XMLHttpRequest;
    if (XHR && !XHR.prototype.__e8TimePatched) {
        XHR.prototype.__e8TimePatched = true;
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
                        processPallets(JSON.parse(this.responseText));
                    }
                } catch (e) {}
            });
            return origSend.apply(this, args);
        };
    }
})();


// ==================================================================
// ОСНОВНОЙ МОДУЛЬ КАЛЬКУЛЯТОРА
// ==================================================================
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
    orderFilter: '',
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
        if (typeof s.orderFilter === 'string') settings.orderFilter = s.orderFilter;
      }
    } catch (e) {}
  }
  function saveSettings() {
    try { localStorage.setItem(LS_SETTINGS_KEY, JSON.stringify(settings)); } catch (e) {}
  }

  const FORMULA = (holes, pallets) =>
    round2(holes * settings.rate * (1 - settings.tax / 100) * pallets);

      // ===== Исключения для отдельных позиций =====
  // Б002<ЦВЕТ> — ставка 0.50 ₽/отверстие, без НДФЛ
  const B002_COLORS = ['Бетон', 'Сонома', 'Молочный', 'Графит', 'Белый', 'Красный'];
  const B002_RATE = 0.50;

  function getSpecialRule(label) {
    const clean = String(label || '').trim();
    if (!clean.startsWith('Б002')) return null;
    const rest = clean.slice(4);
    for (const color of B002_COLORS) {
      if (rest.startsWith(color)) {
        return { prefix: 'Б002' + color, rate: B002_RATE, taxFree: true };
      }
    }
    return null;
  }

  function calcSum(label, holes, pallets) {
    if (holes == null) return 0;
    const rule = getSpecialRule(label);
    if (rule) {
      return round2(holes * rule.rate * pallets);
    }
    return round2(holes * settings.rate * (1 - settings.tax / 100) * pallets);
  }

  const log = (...a) => console.log('%c[e8-calc]', 'color:#09f;font-weight:bold', ...a);

  function round2(n) { return Math.round((Number(n) + Number.EPSILON) * 100) / 100; }

  function sortIsoAsc(arr) {
    return arr.slice().sort((a, b) => new Date(a).getTime() - new Date(b).getTime());
  }

  function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
    }[c]));
  }
  const escapeAttr = escapeHtml;

  function parseOrderFilter(str) {
    const s = String(str || '').trim();
    if (!s) return null;
    const m = s.match(/^(\d+)\s*[-–—]\s*(\d+)$/);
    if (m) {
      const a = Number(m[1]), b = Number(m[2]);
      return { type: 'range', from: Math.min(a, b), to: Math.max(a, b) };
    }
    const parts = s.split(/[,\s;]+/).filter(Boolean);
    if (parts.length) {
      const nums = parts.map(x => Number(String(x).replace(/\D/g, ''))).filter(Number.isFinite);
      if (nums.length) return { type: 'list', set: new Set(nums) };
    }
    return null;
  }

  function orderMatchesFilter(label, spec) {
    if (!spec) return true;
    const m = String(label || '').match(/(\d+)/);
    if (!m) return true;
    const num = Number(m[1]);
    if (spec.type === 'range') return num >= spec.from && num <= spec.to;
    if (spec.type === 'list')  return spec.set.has(num);
    return true;
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

    const orderFilterSpec = parseOrderFilter(settings.orderFilter);

    for (const order of items) {
      if (!orderMatchesFilter(order.label, orderFilterSpec)) continue;

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

          const sum = calcSum(label, holes, palletsInRange);
          totalPallets += palletsInRange;
          totalSum = round2(totalSum + sum);
          orderStat.pallets += palletsInRange;
          orderStat.sum = round2(orderStat.sum + sum);

         // Статистика — с защитой по palletId

          if (window.__e8Stats && window.__e8Stats.accumulate) {
              const rule = getSpecialRule(label);
              const earnPerPallet = rule
                  ? holes * rule.rate
                  : holes * settings.rate * (1 - settings.tax / 100);
              for (let i = 0; i < palletsInRange; i++) {
                  const child = childList[i] || null;
                  const palletId = child?.id || null;
                  window.__e8Stats.accumulate(label, holes, earnPerPallet, palletId);
              }
          }

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
    #e8-calc-box{position:fixed;right:20px;bottom:20px;left:auto;z-index:2147483647;width:360px;max-width:calc(100vw - 24px);box-sizing:border-box;background:#1e1e2e;color:#eee;border:1px solid #444;border-radius:10px;font:13px/1.4 system-ui,sans-serif;box-shadow:0 8px 24px rgba(0,0,0,.5);padding:12px 14px;pointer-events:auto}
    #e8-calc-box.hidden{display:none}
    #e8-calc-box .btn-row{display:flex;gap:6px;margin-top:8px}
    #e8-calc-box .btn-row button{margin-top:0;flex:1;width:auto}
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

    #e8-calc-dates,#e8-calc-settings,#e8-stats-panel,#e8-online-panel{display:none;margin-top:8px;padding-top:8px;border-top:1px dashed #444}
    #e8-stats-panel{background:#161622;border-left:2px solid #334155;border-radius:0 4px 4px 0;padding:6px 8px;font-size:12px;max-height:400px;overflow-y:auto;box-sizing:border-box}
    #e8-calc-dates label,#e8-calc-settings label{display:block;font-size:11px;opacity:.8;margin:4px 0 2px}
    #e8-calc-dates input,#e8-calc-settings input{width:100%;box-sizing:border-box;background:#0f172a;color:#eee;border:1px solid #334155;border-radius:5px;padding:4px 6px;font:12px system-ui;color-scheme:dark}
    #e8-calc-dates .btns,#e8-calc-settings .btns{display:flex;gap:6px;margin-top:8px}
    #e8-calc-dates .btns button,#e8-calc-settings .btns button{margin-top:0;flex:1}
    #e8-calc-settings input[type=range]{padding:0;background:transparent;border:0}
    #e8-calc-settings input[type=checkbox]{width:auto;margin:0}
    #e8-calc-settings .hint{font-size:10px;opacity:.6;margin-top:2px}
    #e8-calc-settings .chk{display:flex;align-items:center;gap:6px;margin-top:8px;font-size:11px;opacity:.9}
    #e8-online-panel{background:#161622;border-left:2px solid #334155;border-radius:0 4px 4px 0;padding:6px 8px;font-size:11px;max-height:400px;overflow-y:auto;box-sizing:border-box}
    #e8-online-panel table{width:100%;border-collapse:collapse;table-layout:fixed}
    #e8-online-panel th{text-align:left;font-size:10px;opacity:.6;padding:2px 3px;border-bottom:1px solid #2a2a3e;font-weight:600}
    #e8-online-panel td{padding:3px;vertical-align:top;border-bottom:1px solid #1e1e2e;word-wrap:break-word;overflow-wrap:break-word;font-size:11px}
    #e8-online-panel tr:hover td{background:#1e1e2e}
    #e8-online-panel .status-on{color:#7CFC9B;font-weight:600}
    #e8-online-panel .status-off{color:#fbbf24}
    #e8-online-panel .col-status{width:52px}
    #e8-online-panel .col-name{width:38%}
    #e8-online-panel .col-work{width:28%}
    #e8-online-panel .col-comment{width:28%;opacity:.85}

    #e8-calc-mini{
      position:fixed;right:20px;bottom:20px;z-index:999999;
      width:46px;height:46px;border-radius:50%;
      border:0;cursor:pointer;
      box-shadow:0 4px 14px rgba(0,0,0,.5);
      display:flex;align-items:center;justify-content:center;
      transition:background-color .15s, transform .1s;
      color:transparent;
      background-color:#3b82f6;
      background-image:
        url('https://raw.githubusercontent.com/chucpe/qa52/master/icon.png'),
        url('data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22white%22%20stroke-width%3D%222%22%20stroke-linecap%3D%22round%22%20stroke-linejoin%3D%22round%22%3E%3Cpath%20d%3D%22M18%207V4H6l6%208-6%208h12v-3%22%2F%3E%3C%2Fsvg%3E');
      background-repeat: no-repeat, no-repeat;
      background-position: center center, center center;
      background-size: 26px 26px, 22px 22px;
    }
    #e8-calc-mini:hover{background-color:#2563eb;transform:scale(1.05)}
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

        <label>Заявки (напр. 30-40 или 30,32,35):</label>
        <input type="text" id="e8-order-filter" placeholder="пусто = все" value="${escapeAttr(settings.orderFilter)}">

        <label class="chk">
          <input type="checkbox" id="e8-automin" ${settings.autoMinimize ? 'checked' : ''}>
          Автоматически сворачивать после загрузки
        </label>
        <div style="display:flex;justify-content:space-between;font-size:11px;opacity:.85;margin-top:8px;">
          <span>localStorage:</span>
          <span id="e8-storage-info" style="font-weight:600;">— MB / —%</span>
        </div>

        <div class="btns">
          <button id="e8-settings-apply">Применить</button>
          <button id="e8-settings-reset" class="secondary">Сбросить</button>
        </div>
      </div>
      <div id="e8-online-panel" style="display:none;"></div>
      <div id="e8-stats-panel" style="display:none;"></div>
      <button id="e8-calc-toggle">Считать по датам</button>
      <button id="e8-calc-recalc" class="secondary">Пересчитать</button>
      <div class="btn-row">
      <button id="e8-calc-stats-toggle" class="secondary">Статистика</button>
      <button id="e8-calc-settings-toggle" class="secondary">Настройка</button>
      </div>
      <div class="btn-row">
      <button id="e8-calc-online-toggle" class="secondary">Онлайн</button>
      <button id="e8-calc-minimize" class="secondary">Свернуть</button>
      </div>
    `;
           // Функция для безопасной вставки
    function safeAppend() {
        if (!document.body) {
            setTimeout(safeAppend, 50);
            return;
        }
        // Проверяем, не вставили ли уже (на случай повторных вызовов)
        if (!document.getElementById('e8-calc-box')) {
            document.body.appendChild(box);
        }

        let mini = document.getElementById('e8-calc-mini');
        if (!mini) {
            mini = document.createElement('button');
            mini.id = 'e8-calc-mini';
            mini.title = 'Развернуть подсчёт';
            document.body.appendChild(mini);
            mini.addEventListener('click', () => setMinimized(false));
        }

        applyAdaptiveLayout();

        if (settings.autoMinimize) {
            setMinimized(true);
        } else {
            const savedMin = localStorage.getItem(LS_MIN_KEY) === '1';
            setMinimized(savedMin);
        }
    }
              // ← Восстанавливаем свёрнутость ПОСЛЕ вставки в DOM
        if (settings.autoMinimize) {
            setMinimized(true);
        } else {
            const savedMin = localStorage.getItem(LS_MIN_KEY) === '1';
            setMinimized(savedMin);
        }

    if (document.readyState === 'complete' || document.readyState === 'interactive') {
        setTimeout(safeAppend, 300);
    } else {
        window.addEventListener('DOMContentLoaded', () => setTimeout(safeAppend, 500));
    }

    const datesPanel = box.querySelector('#e8-calc-dates');
    const settingsPanel = box.querySelector('#e8-calc-settings');

    box.querySelector('#e8-calc-toggle').addEventListener('click', () => {
      const visible = datesPanel.style.display === 'block';
      datesPanel.style.display = visible ? 'none' : 'block';
      settingsPanel.style.display = 'none';
      const statsPanel = document.getElementById('e8-stats-panel');
      if (statsPanel) statsPanel.style.display = 'none';
      const onlinePanel = document.getElementById('e8-online-panel');
      if (onlinePanel) onlinePanel.style.display = 'none';
      const onlineBtn = document.getElementById('e8-calc-online-toggle');
      if (onlineBtn) onlineBtn.textContent = 'Онлайн';

      // Сбрасываем галочки у других кнопок
      const statsBtn = document.getElementById('e8-calc-stats-toggle');
      if (statsBtn) statsBtn.textContent = 'Статистика';
      const settingsBtn = document.getElementById('e8-calc-settings-toggle');
      if (settingsBtn) settingsBtn.textContent = 'Настройка';

      // Ставим/снимаем галочку у себя
      box.querySelector('#e8-calc-toggle').textContent = visible
        ? 'Считать по датам'
        : 'Считать по датам ✓';
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
      const statsPanel = document.getElementById('e8-stats-panel');
      if (statsPanel) statsPanel.style.display = 'none';

      // Сбрасываем галочки у других кнопок
      const statsBtn = document.getElementById('e8-calc-stats-toggle');
      if (statsBtn) statsBtn.textContent = 'Статистика';
      box.querySelector('#e8-calc-toggle').textContent = 'Считать по датам';

      // Ставим/снимаем галочку у себя
      box.querySelector('#e8-calc-settings-toggle').textContent = visible
        ? 'Настройка'
        : 'Настройка ✓';
    if (!visible) renderStorageInfo();
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

    box.querySelector('#e8-settings-apply').addEventListener('click', () => {
      const rate = Number(box.querySelector('#e8-rate-input').value);
      const tax  = Number(box.querySelector('#e8-tax-input').value);
      if (Number.isFinite(rate) && rate > 0) settings.rate = rate;
      if (Number.isFinite(tax)  && tax >= 0 && tax < 100) settings.tax = tax;
      settings.autoMinimize = box.querySelector('#e8-automin').checked;
      settings.orderFilter  = box.querySelector('#e8-order-filter').value.trim();
      saveSettings();
      log('Настройки применены:', settings);
      if (lastJson) processJobs(lastJson);
    });

    box.querySelector('#e8-settings-reset').addEventListener('click', () => {
      settings = { opacity: 0.95, rate: 0.48, tax: 13, autoMinimize: settings.autoMinimize, orderFilter: '' };
      box.querySelector('#e8-op-range').value = 95;
      box.querySelector('#e8-op-val').textContent = '95%';
      box.querySelector('#e8-rate-input').value = 0.48;
      box.querySelector('#e8-tax-input').value  = 13;
      box.querySelector('#e8-order-filter').value = '';
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
        const { label, g } = entry;
        if (idx > 0) detailHtml += `<div class="pal-sep"></div>`;
        detailHtml += `<div class="pal-head"><b>${escapeHtml(label)}</b> × ${g.pallets} <span class="unk-reason">(${[...g.reasons].join(', ')})</span></div>`;
        if (g.times.length === 0) {
          detailHtml += `<div class="pal"><span class="t">время: —</span></div>`;
        } else {
          for (const iso of g.times) {
            detailHtml += `<div class="pal"><span class="t">${toMskString(iso)}</span><span class="n">${escapeHtml(label)}</span></div>`;
          }
        }
      });

      unknownHtml = `
        <div class="row clickable" data-target="${key}">
          <span>Не посчитано:</span>
          <span class="val">
            <span class="arr" style="opacity:.6;margin-right:4px;display:inline-block">▶</span><span class="unk-red">${unknownPallets}</span>
          </span>
        </div>
        <div class="ord-detail" id="${key}">${detailHtml}</div>
      `;
    } else {
      unknownHtml = `<div class="row"><span>Не посчитано:</span><span class="val">0</span></div>`;
    }

    const filterLine = (filter.from || filter.to || settings.orderFilter)
      ? `<div style="font-size:11px;opacity:.6;margin-top:4px">${
          (filter.from || filter.to)
            ? `Даты: ${filter.from ? toMskString(new Date(filter.from).toISOString()) : '…'} — ${filter.to ? toMskString(new Date(filter.to).toISOString()) : '…'}`
            : ''
        }${
          settings.orderFilter
            ? `${(filter.from || filter.to) ? ' · ' : ''}Заявки: ${escapeHtml(settings.orderFilter)}`
            : ''
        }</div>`
      : '';

    body.innerHTML = `
      <div class="row"><span>Всего паллет:</span><span class="val">${totalPalletsAll ?? totalPallets}</span></div>
      <div class="row"><span>Посчитано паллет:</span><span class="val">${totalPallets}</span></div>
      ${unknownHtml}
      <div class="row"><span>Сумма:</span><span class="val sum">${money(totalSum)}</span></div>
      ${filterLine}
      <div id="e8-calc-orders" style="${ordersHtml ? '' : 'display:none;'}">${ordersHtml ? `<div class="ord">${ordersHtml}</div>` : ''}</div>
    `;

    body.querySelectorAll('.ord-head, .row.clickable').forEach(head => {
      head.addEventListener('click', () => {
        const id = head.getAttribute('data-target');
        const detail = body.querySelector('#' + id);
        const isOpen = head.classList.toggle('open');
        if (detail) detail.classList.toggle('open', isOpen);
      });
    });
  }

  loadSettings();
  loadPrice().catch(e => log('Ошибка загрузки прайса:', e));

    document.addEventListener('change', (e) => {
    if (e.target && e.target.id === 'e8-automin') {
      settings.autoMinimize = e.target.checked;
      saveSettings();
      log('Автосворачивание:', settings.autoMinimize);
    }
  });

  function boot() {
    ensureBox();
  }

  if (document.body) boot();
  else window.addEventListener('DOMContentLoaded', boot);
})();

// ==================================================================
// МОДУЛЬ A. ФИОЛЕТОВЫЙ ЦВЕТ «Заявка №»
// ==================================================================
(function() {
  'use strict';
  const TARGET_SELECTOR = 'div.text-sm.font-semibold.break-all';
  const ZAYAVKA_PREFIX = 'Заявка №';
  const ZAYAVKA_COLOR = '#a855f7';

  function styleZayavki() {
    document.querySelectorAll(TARGET_SELECTOR).forEach(node => {
      const text = (node.textContent || '').trim();
      if (!text.startsWith(ZAYAVKA_PREFIX)) return;
      if (node.dataset.zayavkaStyled === '1') return;
      const currentSize = parseFloat(getComputedStyle(node).fontSize) || 14;
      node.style.setProperty('font-size', (currentSize + 1) + 'px', 'important');
      node.style.setProperty('color', ZAYAVKA_COLOR, 'important');
      node.dataset.zayavkaStyled = '1';
    });
  }
  function start() {
    styleZayavki();
    const observer = new MutationObserver(() => styleZayavki());
    observer.observe(document.body || document.documentElement, { childList: true, subtree: true });
    setInterval(styleZayavki, 1000);
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', start);
  else start();
})();

// ==================================================================
// МОДУЛЬ B. ПОДПИСЬ ЦЕНЫ СПРАВА ОТ НАЗВАНИЯ
// ==================================================================
(function() {
  'use strict';
  const PRICE_URL = 'https://raw.githubusercontent.com/chucpe/qa52/master/src/price.json';
  const TARGET_SELECTOR = 'div.text-sm.font-semibold.break-all';
  const DECOR_ATTR = 'data-price-decorated';
  const RATE = 0.48;
  const TAX_MULTIPLIER = 0.87;
  const B002_COLORS = ['Бетон', 'Сонома', 'Молочный', 'Графит', 'Белый', 'Красный'];
  const B002_RATE = 0.50;

  function isB002Color(name) {
    if (!name.startsWith('Б002')) return false;
    const rest = name.slice(4);
    return B002_COLORS.some(c => rest.startsWith(c));
  }
  const PRICE_COLOR = '#7dd87d';
  let priceByName = {};
  let priceLoaded = false;

  function loadPrices() {
    return new Promise((resolve) => {
      GM_xmlhttpRequest({
        method: 'GET',
        url: PRICE_URL + '?t=' + Date.now(),
        onload: (resp) => {
          if (resp.status !== 200) { resolve(false); return; }
          try {
            const data = JSON.parse(resp.responseText);
            if (!Array.isArray(data)) throw new Error('not array');
            priceByName = {};
                        data.forEach(item => {
              if (!item || !item.name) return;
              if (typeof item.holes !== 'number') return;

              const name = item.name.trim();
              let price;

              if (isB002Color(name)) {
                // Б002<цвет>: ставка 0.50 ₽ за отверстие, без НДФЛ
                price = item.holes * B002_RATE;
              } else {
                // Обычная формула с НДФЛ
                price = item.holes * RATE * TAX_MULTIPLIER;
              }
              priceByName[name] = price;
            });
            priceLoaded = true;
            resolve(true);
          } catch (e) { resolve(false); }
        },
        onerror: () => resolve(false),
        ontimeout: () => resolve(false),
        timeout: 10000
      });
    });
  }
  function normalize(str) { return (str || '').replace(/\s+/g, ' ').trim().toLowerCase(); }
  function findPriceByText(text) {
    const t = normalize(text);
    if (!t) return null;
    for (const [name, price] of Object.entries(priceByName)) if (normalize(name) === t) return { name, price };
    for (const [name, price] of Object.entries(priceByName)) if (normalize(name).startsWith(t + ' ')) return { name, price };
    for (const [name, price] of Object.entries(priceByName)) if (t.startsWith(normalize(name) + ' ')) return { name, price };
    return null;
  }
  function decorate() {
    if (!priceLoaded) return;
    document.querySelectorAll(TARGET_SELECTOR).forEach(node => {
      if (node.hasAttribute(DECOR_ATTR)) return;
      const text = (node.textContent || '').trim();
      if (!text) return;
      const match = findPriceByText(text);
      if (!match) return;
      const span = document.createElement('span');
      span.style.cssText = `color:${PRICE_COLOR}; font-weight:bold; margin-left:6px;`;
      span.textContent = `(${match.price.toFixed(2)})`;
      node.appendChild(span);
      node.setAttribute(DECOR_ATTR, '1');
    });
  }
  loadPrices().then((ok) => {
    if (!ok) return;
    decorate();
    const observer = new MutationObserver(() => decorate());
    observer.observe(document.body || document.documentElement, { childList: true, subtree: true });
    let lastUrl = location.href;
    setInterval(() => {
      if (location.href !== lastUrl) { lastUrl = location.href; setTimeout(decorate, 300); }
    }, 500);
  });
})();

// ==================================================================
// УСТАНОВКА ПРИЛОЖЕНИЯ — СКРЫВАЕМ БАННЕР
// ==================================================================
(function() {
    'use strict';
    const DESCRIPTION_SELECTOR = 'div[data-slot="description"]';
    const DESCRIPTION_TEXT = 'Вы можете установить систему как приложение';
    const MARK = 'data-e8-banner-hidden';

    function findBannerContainer(start) {
        let el = start;
        for (let i = 0; i < 8 && el; i++) {
            const st = getComputedStyle(el);
            if (st.position === 'fixed' || st.position === 'sticky') return el;
            el = el.parentElement;
        }
        return start.parentElement?.parentElement || start.parentElement || start;
    }
    function hideBanner() {
        document.querySelectorAll(DESCRIPTION_SELECTOR).forEach(node => {
            const text = (node.textContent || '').trim();
            if (!text.includes(DESCRIPTION_TEXT)) return;
            const container = findBannerContainer(node);
            if (!container || container.dataset.e8BannerHidden === '1') return;
            container.style.setProperty('display', 'none', 'important');
            container.setAttribute(MARK, '1');
        });
    }
    function start() {
        hideBanner();
        const observer = new MutationObserver(() => hideBanner());
        observer.observe(document.body || document.documentElement, { childList: true, subtree: true });
        setInterval(hideBanner, 500);
        const reobserve = () => {
            try { observer.disconnect(); } catch (e) {}
            if (document.body) observer.observe(document.body, { childList: true, subtree: true });
            hideBanner();
        };
        let lastHref = location.href;
        setInterval(() => {
            if (location.href !== lastHref) { lastHref = location.href; reobserve(); }
        }, 300);
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', start);
    else start();
})();

// ==================================================================
// СКРЫВАЕМ БАННЕР «Напоминание»
// ==================================================================
(function() {
    'use strict';
    const POPUP_TITLE = 'Напоминание';
    function closeReminderPopup() {
        const candidates = document.querySelectorAll('div.reka-dialog-content, div[role="dialog"], dialog[open]');
        for (const dlg of candidates) {
            const text = (dlg.textContent || '');
            if (!text.includes(POPUP_TITLE)) continue;
            const selectors = ['button[aria-label="Закрыть"]', 'button[data-slot="close"]', 'button[type="button"]'];
            for (const sel of selectors) {
                const btn = dlg.querySelector(sel);
                if (btn && !btn.disabled) { btn.click(); return; }
            }
        }
    }
    function start() {
        closeReminderPopup();
        const observer = new MutationObserver(() => closeReminderPopup());
        observer.observe(document.body || document.documentElement, { childList: true, subtree: true });
        setInterval(closeReminderPopup, 500);
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', start);
    else start();
})();

// ==================================================================
// ЗАПРЕЩАЕМ ОТКРЫТИЕ КНОПКИ «Характеристика продукции»
// ==================================================================
(function() {
    'use strict';
    const style = document.createElement('style');
    style.id = 'calc-hide-khar-style';
    style.textContent = `
        div[role="dialog"]:has(#reka-dialog-title-v1),
        div.reka-dialog-content:has(#reka-dialog-title-v1) {
            visibility: hidden !important;
            opacity: 0 !important;
            pointer-events: none !important;
            transform: scale(0.001) !important;
        }
        div[data-slot="overlay"][data-state="open"] {
            visibility: hidden !important;
            opacity: 0 !important;
            pointer-events: none !important;
        }
    `;
    (document.head || document.documentElement).appendChild(style);
})();

// ==================================================================
// ПОДСЧЁТ КОЛИЧЕСТВА ОТВЕРСТИЙ И ЦИТАТА
// ==================================================================
(function() {
    'use strict';
    const W = (typeof unsafeWindow !== 'undefined') ? unsafeWindow : window;
    const STORAGE_KEY_SUM    = 'e8_hole_counter_sum';
    const STORAGE_KEY_PALLET = 'e8_pallet_counter_sum';
    let totalSum = 0;
    let palletSum = 0;
    let waitingForCharData = false;

    try {
        const s = localStorage.getItem(STORAGE_KEY_SUM);
        if (s !== null) { const p = parseInt(s, 10); if (!isNaN(p)) totalSum = p; }
        const p2 = localStorage.getItem(STORAGE_KEY_PALLET);
        if (p2 !== null) { const p = parseInt(p2, 10); if (!isNaN(p)) palletSum = p; }
    } catch (e) {}

    function saveSum()    { try { localStorage.setItem(STORAGE_KEY_SUM, String(totalSum)); } catch (e) {} }
    function savePallet() { try { localStorage.setItem(STORAGE_KEY_PALLET, String(palletSum)); } catch (e) {} }

    function createQuote() {
        if (document.getElementById('hole-counter-quote')) return;
        const q = document.createElement('div');
        q.id = 'hole-counter-quote';
        q.style.cssText = `
            position: fixed; right: 20px; bottom: 74px; z-index: 999999;
            background: #1e1e2e; color: #eee; border: 1px solid #444; border-radius: 8px;
            padding: 8px 26px 8px 12px; font: 12px/1.4 system-ui, sans-serif;
            box-shadow: 0 6px 18px rgba(0,0,0,.5); min-width: 170px;
            max-width: calc(100vw - 40px); box-sizing: border-box; display: none; user-select: none;
        `;
        q.innerHTML = `
            <span id="hole-counter-quote-close" style="position:absolute; top:4px; right:6px; cursor:pointer; opacity:.6; font-size:14px; line-height:1;" title="Закрыть">×</span>
            <div style="display:flex; justify-content:space-between; gap:14px; color:#7CFC9B;">
                <span>Заработок:</span>
                <span id="hole-counter-earnings" style="font-weight:700;">0.00</span>
            </div>
            <div style="display:flex; justify-content:space-between; gap:14px; margin-top:2px; color:#fbbf24;">
                <span>Отверстий:</span>
                <span id="hole-counter-value" style="font-weight:700;">0</span>
            </div>
            <button id="hole-counter-reset" style="margin-top:8px; width:100%; cursor:pointer; background:#ef4444; color:#fff; border:0; border-radius:5px; padding:4px 8px; font:600 11px system-ui,sans-serif;">Сброс</button>
        `;
        document.body.appendChild(q);
        q.querySelector('#hole-counter-quote-close').addEventListener('click', () => { q.style.display = 'none'; });
        q.querySelector('#hole-counter-reset').addEventListener('click', () => {
            totalSum = 0; palletSum = 0; saveSum(); savePallet(); updateDisplay();
        });
        updateDisplay();
    }

    function updateDisplay() {
        const earningsSpan = document.getElementById('hole-counter-earnings');
        const valueSpan    = document.getElementById('hole-counter-value');
        if (earningsSpan) earningsSpan.textContent = (totalSum * 0.48 * 0.87).toFixed(2);
        if (valueSpan)    valueSpan.textContent    = totalSum;
    }
    function showQuote() {
        const q = document.getElementById('hole-counter-quote');
        if (q) q.style.display = 'block';
    }
    function isRequired(item) {
        if (!item.category || !item.category.label) return false;
        return item.category.label.trim() === 'ЛДСП';
    }
    function traverse(node, acc) {
        if (!node || typeof node !== 'object') return;
        if (typeof node.totalHole === 'number' && isRequired(node)) acc.holes += node.totalHole;
        if (typeof node.count === 'number') acc.pallets += node.count;
        if (Array.isArray(node.children)) node.children.forEach(ch => traverse(ch, acc));
        if (Array.isArray(node.items))    node.items.forEach(ch => traverse(ch, acc));
    }
    function processJsonData(data) {
        if (!data) return;
        const acc = { holes: 0, pallets: 0 };
        traverse(data, acc);
        if (acc.holes > 0)   { totalSum  += acc.holes;   saveSum(); }
        if (acc.pallets > 0) { palletSum += acc.pallets; savePallet(); }
        if (waitingForCharData && (acc.holes > 0 || acc.pallets > 0)) {
            updateDisplay(); showQuote(); waitingForCharData = false;
        } else if (acc.holes > 0 || acc.pallets > 0) {
            updateDisplay();
        }
    }
    document.addEventListener('click', (e) => {
        const target = e.target;
        if (!target || !target.closest) return;
        const btn = target.closest('button, [role="button"], a, div, span, [data-slot="trigger"]');
        if (!btn) return;
        const text = (btn.textContent || '').trim();
        if (!text.includes('Характеристики продукции')) return;
        waitingForCharData = true;
        setTimeout(() => { waitingForCharData = false; }, 5000);
    }, true);
    const originalOpen = W.XMLHttpRequest.prototype.open;
    W.XMLHttpRequest.prototype.open = function(method, url) {
        this.addEventListener('load', function() {
            if (this.status === 200 && this.responseText) {
                try {
                    const json = JSON.parse(this.responseText);
                    if (json && (json.items || json.other)) processJsonData(json);
                } catch (e) {}
            }
        });
        return originalOpen.apply(this, arguments);
    };
    const originalFetch = W.fetch;
    W.fetch = async function(...args) {
        const response = await originalFetch.apply(this, args);
        const clone = response.clone();
        clone.json().then(data => {
            if (data && (data.items || data.other)) processJsonData(data);
        }).catch(() => {});
        return response;
    };
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', createQuote);
    else createQuote();
})();

//Замена иконки

(function() {
    'use strict';

    // Основа
    const MY_ICON_URL = 'https://img.icons8.ru/?size=100&id=71367&format=png&color=000000';

    // Резерв
    const FALLBACK_SVG = encodeURIComponent(
        '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 7V4H6l6 8-6 8h12v-3"/></svg>'
    );
    const FALLBACK_URL = `data:image/svg+xml;utf8,${FALLBACK_SVG}`;

    const css = `
        #e8-calc-mini{
            position:fixed;right:20px;bottom:20px;z-index:999999;
            width:46px;height:46px;border-radius:50%;
            border:0;cursor:pointer;
            box-shadow:0 4px 14px rgba(0,0,0,.5);
            display:flex;align-items:center;justify-content:center;
            transition:background-color .15s, transform .1s;
            color:transparent;

            /*Основа > резерв*/
            background-color:#3b82f6;
            background-image:
                url('${MY_ICON_URL}'),
                url('${FALLBACK_URL}');
            background-repeat: no-repeat, no-repeat;
            background-position: center center, center center;
            background-size: 26px 26px, 22px 22px;
        }

        #e8-calc-mini:hover{
            background-color:#2563eb;
            transform:scale(1.05);
        }

        #e8-calc-mini.hidden{ display:none; }
    `;

    const style = document.createElement('style');
    style.textContent = css;
    document.head.appendChild(style);
})();
  // ===== Информер localStorage =====
  const STORAGE_LIMIT_MB = 5; // ~5 MB стандартный лимит браузера

  function getTotalLocalStorageBytes() {
    let total = 0;
    try {
      for (let i = 0; i < localStorage.length; i++) {
        const k = localStorage.key(i);
        if (!k) continue;
        const v = localStorage.getItem(k) || '';
        // UTF-16: 2 байта на символ
        total += (k.length + v.length) * 2;
      }
    } catch (e) {}
    return total;
  }

  function renderStorageInfo() {
    const el = document.getElementById('e8-storage-info');
    if (!el) return;

    const bytes = getTotalLocalStorageBytes();
    const mb = bytes / 1024 / 1024;
    const percent = Math.min(100, (mb / STORAGE_LIMIT_MB) * 100);

    el.textContent = `${mb.toFixed(2)} MB / ${percent.toFixed(1)}%`;

    // Цвет в зависимости от заполнения
    if (percent < 50) el.style.color = '#7CFC9B';       // зелёный
    else if (percent < 80) el.style.color = '#fbbf24';  // жёлтый
    else el.style.color = '#ff5555';                    // красный
  }

// ==================================================================
// МОДУЛЬ «ОНЛАЙН» — кто сейчас работает
// ==================================================================
(function() {
  'use strict';

  const W = (typeof unsafeWindow !== 'undefined') ? unsafeWindow : window;
  const EMPLOYEES_URL = 'https://raw.githubusercontent.com/chucpe/qa52/refs/heads/master/src/employees.json';
  const API_BASE = 'https://backoffice-e8.ffa.su';

  let usersCache = [];
  let currentWorkPlanId = null;
  let employeesMap = null;

  // ===== 1. Загрузка employees.json =====
  function loadEmployees(force) {
    return new Promise((resolve) => {
      if (force) {
        employeesMap = null;
      } else if (employeesMap) {
        resolve(employeesMap);
        return;
      }

      const cacheBuster = '?t=' + Date.now();

      GM_xmlhttpRequest({
        method: 'GET',
        url: EMPLOYEES_URL + cacheBuster,
        nocache: true,
        headers: {
          'Cache-Control': 'no-cache, no-store, must-revalidate',
          'Pragma': 'no-cache',
          'Expires': '0',
        },
        onload: (res) => {
          if (res.status !== 200) {
            resolve(new Map());
            return;
          }
          try {
            let text = res.responseText
              .replace(/,\s*}/g, '}')
              .replace(/,\s*]/g, ']');
            const arr = JSON.parse(text);
            const map = new Map();
            const list = Array.isArray(arr) ? arr : Object.values(arr);
            for (const emp of list) {
              if (!emp) continue;
              const key = String(emp.number ?? emp.id ?? '');
              if (!key) continue;
              map.set(key, {
                firstName: (emp.firstName || '').trim(),
                lastName:  (emp.lastName  || '').trim(),
                workplace: (emp.workplace || emp.machine || '').trim(),
                comments:  (emp.comments  || '').trim(),
              });
            }
            employeesMap = map;
            resolve(map);
          } catch (e) {
            resolve(new Map());
          }
        },
        onerror: () => resolve(new Map()),
        ontimeout: () => resolve(new Map()),
        timeout: 10000,
      });
    });
  }

  // ===== 2. Получение токена =====
  function getToken() {
    try {
      let t = W.__e8Token || window.__e8Token;
      if (!t) {
        t = localStorage.getItem('auth_token');
      }
      if (!t) return null;
      if (/^Bearer\s+/i.test(t)) return t;
      if (/^eyJ/.test(t)) return 'Bearer ' + t;
      return null;
    } catch (e) { return null; }
  }

  // ===== 3. Прямой запрос /1/jobs?statusId=1 для получения workPlanId =====
  function fetchWorkPlanId() {
    return new Promise((resolve) => {
      const token = getToken();
      if (!token) { resolve(null); return; }
      const url = `${API_BASE}/api/v1/admin/projects/1/jobs?statusId=1`;

      GM_xmlhttpRequest({
        method: 'GET',
        url,
        headers: {
          'Accept': 'application/json',
          'Authorization': token,
        },
        onload: (res) => {
          if (res.status !== 200) { resolve(null); return; }
          try {
            const j = JSON.parse(res.responseText);

            // Приоритет 1: из справочника workPlans
            const workPlans = j?.other?.dictionary?.workPlans
                           || j?.data?.other?.dictionary?.workPlans
                           || [];
            if (Array.isArray(workPlans) && workPlans.length > 0 && workPlans[0].id) {
              resolve(workPlans[0].id);
              return;
            }

            // Приоритет 2: рекурсивный поиск workPlanId
            function findWpId(obj, depth) {
              if (depth > 6 || !obj || typeof obj !== 'object') return null;
              if (obj.workPlanId && typeof obj.workPlanId === 'string') return obj.workPlanId;
              if (Array.isArray(obj)) {
                for (const item of obj) {
                  const r = findWpId(item, depth + 1);
                  if (r) return r;
                }
              } else {
                for (const k of Object.keys(obj)) {
                  const v = obj[k];
                  if (v && typeof v === 'object') {
                    const r = findWpId(v, depth + 1);
                    if (r) return r;
                  }
                }
              }
              return null;
            }
            resolve(findWpId(j, 0));
          } catch (e) {
            resolve(null);
          }
        },
        onerror: () => resolve(null),
        ontimeout: () => resolve(null),
        timeout: 15000,
      });
    });
  }

  // ===== 4. Запрос одной страницы пользователей =====
  function fetchUsersPage(workPlanId, page) {
    return new Promise((resolve) => {
      const token = getToken();
      if (!token) { resolve([]); return; }

      const url = `${API_BASE}/api/v1/admin/dictionary/users?page=${page}&take=20&projectId=1&workPlanId=${workPlanId}&label`;

      GM_xmlhttpRequest({
        method: 'GET',
        url,
        headers: {
          'Accept': 'application/json',
          'Authorization': token,
        },
        onload: (res) => {
          if (res.status !== 200) { resolve([]); return; }
          try {
            const j = JSON.parse(res.responseText);
            const items = j?.items || j?.data?.items || j?.data || [];
            resolve(Array.isArray(items) ? items : []);
          } catch (e) {
            resolve([]);
          }
        },
        onerror: () => resolve([]),
        ontimeout: () => resolve([]),
        timeout: 15000,
      });
    });
  }

  // ===== 5. Загрузка двух страниц пользователей =====
  async function loadUsers(workPlanId) {
    const [page1, page2] = await Promise.all([
      fetchUsersPage(workPlanId, 1),
      fetchUsersPage(workPlanId, 2),
    ]);
    usersCache = [...page1, ...page2];
    return usersCache;
  }

  // ===== 6. Форматирование lastSeen в МСК (+3 часа) =====
  function formatLastSeen(iso) {
    if (!iso) return '—';
    let s = String(iso);
    if (!/[Zz]|[+\-]\d{2}:?\d{2}$/.test(s)) s = s + 'Z';
    const d = new Date(s);
    if (isNaN(d)) return '—';
    const msk = new Date(d.getTime() + 3 * 60 * 60 * 1000);
    const p = (x) => String(x).padStart(2, '0');
    const day = p(msk.getUTCDate());
    const mon = p(msk.getUTCMonth() + 1);
    const yr = String(msk.getUTCFullYear()).slice(-2);
    const hh = p(msk.getUTCHours());
    const mm = p(msk.getUTCMinutes());
    return `${hh}:${mm} ${day}.${mon}.${yr}`;
  }

  function escapeHtmlOnline(s) {
    return String(s || '').replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
    }[c]));
  }

  // ===== 7. Рендер таблицы =====
  function renderOnlineTable() {
    const panel = document.getElementById('e8-online-panel');
    if (!panel) return;

    if (usersCache.length === 0) {
      panel.innerHTML = '<div style="font-size:11px;opacity:.6;padding:6px;">Нет данных</div>';
      return;
    }

    const map = employeesMap || new Map();

    const rows = usersCache
      .filter((u) => {
        const empKey = String(u.number ?? u.id ?? '');
        return map.has(empKey);
      })
      .map((u) => {
        const empKey = String(u.number ?? u.id ?? '');
        const emp = map.get(empKey) || {};

        const firstName = (emp.firstName || u.firstName || '').trim();
        const lastName  = (emp.lastName  || u.lastName  || '').trim();
        const fio = [lastName, firstName].filter(Boolean).join(' ') || 'Неизвестный';

        const workplaceRaw = (emp.workplace || '').trim();
        const workplaceHtml = workplaceRaw
          ? escapeHtmlOnline(workplaceRaw)
          : '<span style="font-size:10px;color:#888;font-style:italic;">Не назначено</span>';

        const comments  = (emp.comments  || '').trim();

        const status = String(u.status || '').toUpperCase();
        const isOnline = status === 'ONLINE';

        let statusHtml;
        let lastSeenMs = 0;
        if (isOnline) {
          statusHtml = '<span class="status-on">В сети</span>';
        } else {
          statusHtml = `<span class="status-off">${formatLastSeen(u.lastSeen)}</span>`;
          if (u.lastSeen) {
            const s = String(u.lastSeen);
            const d = new Date(/[Zz]|[+\-]\d{2}:?\d{2}$/.test(s) ? s : s + 'Z');
            if (!isNaN(d)) lastSeenMs = d.getTime();
          }
        }

        return { fio, statusHtml, workplace: workplaceHtml, comments, isOnline, lastSeenMs };
      });

    rows.sort((a, b) => {
      if (a.isOnline !== b.isOnline) {
        return a.isOnline ? -1 : 1;
      }
      if (a.isOnline) {
        return a.fio.localeCompare(b.fio, 'ru');
      }
      return (b.lastSeenMs || 0) - (a.lastSeenMs || 0);
    });

    let html = `
      <table>
        <thead>
          <tr>
            <th class="col-status">Статус</th>
            <th class="col-name">Работник</th>
            <th class="col-work">Рабочее место</th>
            <th class="col-comment">Комментарий</th>
          </tr>
        </thead>
        <tbody>
    `;

    for (const r of rows) {
      html += `
        <tr>
          <td class="col-status">${r.statusHtml}</td>
          <td class="col-name">${escapeHtmlOnline(r.fio)}</td>
          <td class="col-work">${r.workplace}</td>
          <td class="col-comment">${escapeHtmlOnline(r.comments)}</td>
        </tr>
      `;
    }

    html += `</tbody></table>`;
    panel.innerHTML = html;
  }

  // ===== 8. Полное обновление =====
  async function refreshOnline() {
    await loadEmployees(true);
    if (!currentWorkPlanId) {
      currentWorkPlanId = await fetchWorkPlanId();
    }
    if (currentWorkPlanId) {
      await loadUsers(currentWorkPlanId);
    }
    renderOnlineTable();
  }

  // ===== 9. Кнопка «Онлайн» =====
  function attachOnlineButton() {
    const btn = document.getElementById('e8-calc-online-toggle');
    if (!btn || btn.dataset.e8OnlineBound === '1') return;
    btn.dataset.e8OnlineBound = '1';

    btn.addEventListener('click', async () => {
      const panel = document.getElementById('e8-online-panel');
      const statsPanel = document.getElementById('e8-stats-panel');
      const datesPanel = document.getElementById('e8-calc-dates');
      const settingsPanel = document.getElementById('e8-calc-settings');

      if (!panel) return;

      const visible = panel.style.display === 'block';

      const statsBtn = document.getElementById('e8-calc-stats-toggle');
      if (statsBtn) statsBtn.textContent = 'Статистика';
      const settingsBtn = document.getElementById('e8-calc-settings-toggle');
      if (settingsBtn) settingsBtn.textContent = 'Настройка';
      const datesBtn = document.getElementById('e8-calc-toggle');
      if (datesBtn) datesBtn.textContent = 'Считать по датам';

      if (visible) {
        panel.style.display = 'none';
        btn.textContent = 'Онлайн';
        return;
      }

      if (statsPanel) statsPanel.style.display = 'none';
      if (datesPanel) datesPanel.style.display = 'none';
      if (settingsPanel) settingsPanel.style.display = 'none';

      panel.style.display = 'block';
      btn.textContent = 'Онлайн ✓';
      panel.innerHTML = '<div style="font-size:11px;opacity:.6;padding:6px;">Загрузка…</div>';

      try {
        await refreshOnline();
      } catch (e) {
        panel.innerHTML = '<div style="font-size:11px;color:#ff5555;padding:6px;">Ошибка загрузки</div>';
      }
    });
  }

  setInterval(attachOnlineButton, 1000);
  attachOnlineButton();

  // ===== 10. Автоподготовка при старте =====
  setTimeout(async () => {
    await loadEmployees(true);
    if (!currentWorkPlanId) {
      currentWorkPlanId = await fetchWorkPlanId();
    }
  }, 2000);

  window.__e8OnlineRefresh = refreshOnline;
})();
