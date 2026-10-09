// ==UserScript==
// @name         Калькулейшин
// @namespace    http://tampermonkey.net/
// @version      4.2
// @description  Заработок, паллеты и подписи цен + скрытие баннера
// @author       You
// @match        https://e8.ffa.su/*
// @grant        GM_xmlhttpRequest
// @grant        unsafeWindow
// @connect      raw.githubusercontent.com
// @downloadURL https://raw.githubusercontent.com/chucpe/qa52/refs/heads/master/calc.js
// @updateURL https://raw.githubusercontent.com/chucpe/qa52/refs/heads/master/calc.js
// ==/UserScript==

(function() {
    'use strict';

    const W = (typeof unsafeWindow !== 'undefined') ? unsafeWindow : window;

    const STORAGE_KEY_SUM    = 'e8_hole_counter_sum';
    const STORAGE_KEY_PALLET = 'e8_pallet_counter_sum';

    let totalSum = 0;
    let palletSum = 0;

    try {
        const s = localStorage.getItem(STORAGE_KEY_SUM);
        if (s !== null) { const p = parseInt(s, 10); if (!isNaN(p)) totalSum = p; }
        const p2 = localStorage.getItem(STORAGE_KEY_PALLET);
        if (p2 !== null) { const p = parseInt(p2, 10); if (!isNaN(p)) palletSum = p; }
    } catch (e) {}

    function saveSum()    { try { localStorage.setItem(STORAGE_KEY_SUM, String(totalSum)); } catch (e) {} }
    function savePallet() { try { localStorage.setItem(STORAGE_KEY_PALLET, String(palletSum)); } catch (e) {} }

    function createDisplayBox() {
        if (document.getElementById('hole-counter-box')) return;

        const box = document.createElement('div');
        box.id = 'hole-counter-box';
        box.style.cssText = `
            position: fixed; top: 440px; right: 40px;
            background-color: rgba(0, 0, 0, 0.55); color: #fff;
            padding: 12px; border-radius: 8px;
            font-family: Arial, sans-serif; font-size: 14px;
            z-index: 99999; box-shadow: 0 4px 6px rgba(0,0,0,0.3);
            display: flex; flex-direction: column; gap: 6px;
            min-width: 170px; cursor: move; user-select: none;
        `;

        box.innerHTML = `
            <div style="text-align: center; font-size: 12px; opacity: 0.8;">Ваш заработок:</div>
            <div id="hole-counter-earnings" style="text-align: center; font-weight: bold; color: #4ade80; font-size: 22px;">0.00</div>
            <div style="border-top: 1px solid rgba(255,255,255,0.2); margin: 4px 0;"></div>
            <div style="display: flex; justify-content: space-between; font-size: 12px;">
                <span style="opacity: 0.8;">× 0.48:</span>
                <span id="hole-counter-calc1" style="color: #60a5fa; font-weight: bold;">0.00</span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 12px;">
                <span style="opacity: 0.8;">Дырочек:</span>
                <span id="hole-counter-value" style="color: #fbbf24; font-weight: bold;">0</span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 12px;">
                <span style="opacity: 0.8;">Количество паллет:</span>
                <span id="hole-counter-pallets" style="color: #a78bfa; font-weight: bold;">0</span>
            </div>
            <button id="hole-counter-reset" style="margin-top: 6px; padding: 4px 10px; background: #ef4444; color: #fff; border: none; border-radius: 4px; cursor: pointer; font-size: 12px;">Сброс</button>
        `;

        document.body.appendChild(box);

        let isDragging = false, offsetX = 0, offsetY = 0;
        box.addEventListener('mousedown', (e) => {
            if (e.target.id === 'hole-counter-reset') return;
            isDragging = true;
            const rect = box.getBoundingClientRect();
            offsetX = e.clientX - rect.left;
            offsetY = e.clientY - rect.top;
            box.style.right = 'auto';
            box.style.left = rect.left + 'px';
            box.style.top = rect.top + 'px';
            e.preventDefault();
        });
        document.addEventListener('mousemove', (e) => {
            if (!isDragging) return;
            box.style.left = (e.clientX - offsetX) + 'px';
            box.style.top = (e.clientY - offsetY) + 'px';
        });
        document.addEventListener('mouseup', () => { isDragging = false; });

        const resetBtn = document.getElementById('hole-counter-reset');
        if (resetBtn) {
            resetBtn.addEventListener('click', () => {
                totalSum = 0; palletSum = 0;
                saveSum(); savePallet();
                updateDisplay();
            });
        }

        updateDisplay();
    }

    function updateDisplay() {
        const earningsSpan = document.getElementById('hole-counter-earnings');
        const calc1Span    = document.getElementById('hole-counter-calc1');
        const valueSpan    = document.getElementById('hole-counter-value');
        const palletSpan   = document.getElementById('hole-counter-pallets');
        const step1 = totalSum * 0.48;
        const step2 = step1 * 0.87;
        if (earningsSpan) earningsSpan.textContent = step2.toFixed(2);
        if (calc1Span)    calc1Span.textContent    = step1.toFixed(2);
        if (valueSpan)    valueSpan.textContent    = totalSum;
        if (palletSpan)   palletSpan.textContent   = palletSum;
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
        if (acc.holes > 0 || acc.pallets > 0) updateDisplay();
    }

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

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', createDisplayBox);
    } else {
        createDisplayBox();
    }
})();

(function() {
    'use strict';

    const PRICE_URL = 'https://raw.githubusercontent.com/chucpe/qa52/refs/heads/master/src/price.json';
    const TARGET_SELECTOR = 'div.text-sm.font-semibold.break-all';
    const DECOR_ATTR = 'data-price-decorated';
    const DECOR_CLASS = 'price-decorated-span';

    const RATE = 0.48;
    const TAX_MULTIPLIER = 0.87;
    const PRICE_COLOR = '#7dd87d';

    let priceByName = {};
    let priceLoaded = false;

    function loadPrices() {
        return new Promise((resolve) => {
            GM_xmlhttpRequest({
                method: 'GET',
                url: PRICE_URL,
                onload: (resp) => {
                    if (resp.status !== 200) { resolve(false); return; }
                    const trimmed = resp.responseText.trim();
                    if (!trimmed.startsWith('[') && !trimmed.startsWith('{')) { resolve(false); return; }
                    try {
                        const data = JSON.parse(resp.responseText);
                        if (!Array.isArray(data)) throw new Error('not array');
                        priceByName = {};
                        data.forEach(item => {
                            if (!item || !item.name) return;
                            if (typeof item.holes !== 'number') return;
                            priceByName[item.name.trim()] = item.holes * RATE * TAX_MULTIPLIER;
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

    function normalize(str) {
        return (str || '').replace(/\s+/g, ' ').trim().toLowerCase();
    }

    function findPriceByText(text) {
        const t = normalize(text);
        if (!t) return null;
        for (const [name, price] of Object.entries(priceByName)) {
            if (normalize(name) === t) return { name, price };
        }
        for (const [name, price] of Object.entries(priceByName)) {
            const n = normalize(name);
            if (n.startsWith(t + ' ')) return { name, price };
        }
        for (const [name, price] of Object.entries(priceByName)) {
            const n = normalize(name);
            if (t.startsWith(n + ' ')) return { name, price };
        }
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
            span.className = DECOR_CLASS;
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
            if (location.href !== lastUrl) {
                lastUrl = location.href;
                setTimeout(decorate, 300);
            }
        }, 500);
    });
})();

(function() {
    'use strict';

    const TARGET_SELECTOR = 'div.text-sm.font-semibold.break-all';
    const ZAYAVKA_PREFIX = 'Заявка №';
    const ZAYAVKA_COLOR = '#a855f7'; // фиолетовый

    function styleZayavki() {
        document.querySelectorAll(TARGET_SELECTOR).forEach(node => {
            const text = (node.textContent || '').trim();
            if (!text.startsWith(ZAYAVKA_PREFIX)) return;
            if (node.dataset.zayavkaStyled === '1') return;

            // Получаем текущий размер шрифта и увеличиваем на 1px
            const currentSize = parseFloat(getComputedStyle(node).fontSize) || 14;
            node.style.setProperty('font-size', (currentSize + 1) + 'px', 'important');
            node.style.setProperty('color', ZAYAVKA_COLOR, 'important');
            node.dataset.zayavkaStyled = '1';
        });
    }

    function start() {
        styleZayavki();
        const observer = new MutationObserver(() => styleZayavki());
        observer.observe(document.body || document.documentElement, {
            childList: true, subtree: true
        });
        setInterval(styleZayavki, 1000);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', start);
    } else {
        start();
    }
})();

(function() {
    'use strict';

    const BANNER_TEXT = 'Установка приложения';

    function hideBanner() {
        const all = document.querySelectorAll('div, li, section, aside');
        for (const el of all) {
            if (el.children.length > 5) continue;
            if (!el.textContent || !el.textContent.includes(BANNER_TEXT)) continue;
            const rect = el.getBoundingClientRect();
            if (rect.bottom > window.innerHeight - 200 && rect.width < 500) {
                el.style.setProperty('display', 'none', 'important');
            }
        }
    }

    function start() {
        hideBanner();
        const observer = new MutationObserver(() => hideBanner());
        observer.observe(document.body || document.documentElement, {
            childList: true,
            subtree: true
        });
        setInterval(hideBanner, 1000);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', start);
    } else {
        start();
    }
})();

(function() {
    'use strict';

    const POPUP_TITLE = 'Напоминание';

    function closeReminderPopup() {
        // Ищем всевозможные диалоги
        const candidates = document.querySelectorAll(
            'div.reka-dialog-content, div[role="dialog"], dialog[open]'
        );

        for (const dlg of candidates) {
            const text = (dlg.textContent || '');
            if (!text.includes(POPUP_TITLE)) continue;

            // Пробуем 3 селектора кнопки закрытия
            const selectors = [
                'button[aria-label="Закрыть"]',
                'button[data-slot="close"]',
                'button[type="button"]'
            ];

            for (const sel of selectors) {
                const btn = dlg.querySelector(sel);
                if (btn && !btn.disabled) {
                    btn.click();
                    console.log('[calc] Закрыл попап через', sel);
                    return; // выходим после первого успешного клика
                }
            }
        }
    }

    function start() {
        closeReminderPopup();

        const observer = new MutationObserver(() => closeReminderPopup());
        observer.observe(document.body || document.documentElement, {
            childList: true,
            subtree: true
        });

        setInterval(closeReminderPopup, 500);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', start);
    } else {
        start();
    }
})();

