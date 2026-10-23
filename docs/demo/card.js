// ui/card/LabelCardView.kt 의 웹 버전. 크기 단위 1px = 앱의 1dp(=1sp). 카드 논리 크기 360 x 640 / 360 x 360.
'use strict';

const CARD_WIDTH = 360;
const CARD_FORMATS = {
  STORY: { height: 640, label: 'card_format_story' },
  SQUARE: { height: 360, label: 'card_format_square' },
};
const CARD_TEMPLATES = {
  CLASSIC: { label: 'template_classic', free: true, bg: '#EDEBE6', paper: '#FFFFFF', ink: '#111111', rule: '#111111' },
  KRAFT: { label: 'template_kraft', free: false, bg: '#B8956A', paper: '#F7F1E6', ink: '#2B2118', rule: '#2B2118' },
  NIGHT: { label: 'template_night', free: false, bg: '#000000', paper: '#111111', ink: '#F2F2F2', rule: '#F2F2F2' },
};

function el(tag, cls, text) {
  const e = document.createElement(tag);
  if (cls) e.className = cls;
  if (text != null) e.textContent = text;
  return e;
}

function labelColumnWidth(compact) {
  // res/values*/dimens.xml 과 같은 값
  if (I18N.lang === 'en') return compact ? 58 : 74;
  return compact ? 48 : 62;
}

function cardElement(card, templateName, formatName, showWatermark) {
  const t = CARD_TEMPLATES[templateName] || CARD_TEMPLATES.CLASSIC;
  const compact = formatName === 'SQUARE';
  const root = el('div', `lc ${compact ? 'square' : 'story'}`);
  root.style.setProperty('--bg', t.bg);
  root.style.setProperty('--paper', t.paper);
  root.style.setProperty('--ink', t.ink);
  root.style.setProperty('--rule', t.rule);
  root.style.setProperty('--label-col', `${labelColumnWidth(compact)}px`);

  const wrap = el('div', 'lc-wrap');
  const panel = el('div', 'lc-panel');
  panel.append(productHeader(card));
  panel.append(el('div', 'lc-gap1'));
  const info = infoTable(card, compact);
  const nut = hasNutrition(card) ? nutritionBox(card) : null;
  if (compact) {
    const row = el('div', 'lc-row');
    if (info) row.append(info);
    if (nut) row.append(nut);
    panel.append(row);
  } else {
    if (info) panel.append(info);
    if (nut) { panel.append(el('div', 'lc-gap2')); panel.append(nut); }
  }
  wrap.append(panel);
  if (showWatermark) wrap.append(el('div', 'lc-watermark', R('card_watermark')));
  root.append(wrap);
  return root;
}

const shown = (card, key) => !card.hidden.has(key);

function productHeader(card) {
  const f = document.createDocumentFragment();
  f.append(el('div', 'lc-title', R('card_product_name', card.productName)));
  f.append(el('div', 'lc-lot', `${CardText.lot(card.periodStart)}  ${CardText.dateRange(card.periodStart, card.periodEnd)}`));
  return f;
}

function infoTable(card, compact) {
  const rows = [];
  if (!compact) {
    rows.push([R('card_row_product'), R('card_product_name', card.productName)]);
    rows.push([R('card_row_food_type'), R('card_food_type_value')]);
    rows.push([R('card_row_content'), R('fmt_days', card.nutrition.days)]);
  }
  const ings = card.ingredients.filter((i) => shown(card, i.key));
  if (ings.length) rows.push([R('card_row_ingredients'), CardText.ingredients(ings)]);
  if (shown(card, ItemKeys.STORAGE) && card.storage) rows.push([R('card_row_storage'), card.storage.text]);
  if (shown(card, ItemKeys.CAUTION) && card.cautions.length) {
    const c = compact ? card.cautions.slice(0, 1) : card.cautions;
    rows.push([R('card_row_caution'), c.map((p) => p.text).join(' ')]);
  }
  if (shown(card, ItemKeys.EXPIRY) && card.expiry) rows.push([R('card_row_expiry'), card.expiry.text]);
  if (!rows.length) return null;

  const table = el('div', 'lc-info');
  for (const [k, v] of rows) {
    const row = el('div', 'lc-info-row');
    row.append(el('div', 'lc-k', k));
    row.append(el('div', 'lc-v', v));
    table.append(row);
  }
  return table;
}

function hasNutrition(card) {
  return [ItemKeys.SCREEN, ItemKeys.UNLOCKS, ItemKeys.STEPS, ItemKeys.SLEEP].some((k) => shown(card, k));
}

function nutritionBox(card) {
  const n = card.nutrition;
  const rows = [];
  if (shown(card, ItemKeys.SCREEN)) rows.push([R('nutrition_screen_time'), CardText.duration(n.screenMs), CardText.dailyValuePercent(n.screenMs, DailyReference.SCREEN_MS, n.days)]);
  if (shown(card, ItemKeys.UNLOCKS)) rows.push([R('nutrition_unlocks'), R('fmt_times', CardText.count(n.unlocks)), CardText.dailyValuePercent(n.unlocks, DailyReference.UNLOCKS, n.days)]);
  if (shown(card, ItemKeys.STEPS)) rows.push([R('nutrition_steps'), R('fmt_steps', CardText.count(n.steps)), CardText.dailyValuePercent(n.steps, DailyReference.STEPS, n.days)]);
  if (shown(card, ItemKeys.SLEEP)) {
    rows.push([
      R('nutrition_sleep'),
      n.avgSleepMs != null ? CardText.duration(n.avgSleepMs) : R('nutrition_not_measured'),
      n.avgSleepMs != null ? CardText.dailyValuePercent(n.avgSleepMs, DailyReference.SLEEP_MS, 1) : null,
    ]);
  }

  const box = el('div', 'lc-nut');
  const head = el('div', 'lc-nut-head');
  head.append(el('b', null, R('nutrition_title')));
  head.append(el('span', null, R('nutrition_total_content', R('fmt_days', n.days))));
  box.append(head);
  const sub = el('div', 'lc-nut-sub');
  sub.append(el('span', null, R('nutrition_per_total')));
  sub.append(el('span', null, R('nutrition_daily_value_header')));
  box.append(sub);
  box.append(el('div', 'lc-thick'));
  for (const [name, amount, pct] of rows) {
    const r = el('div', 'lc-nut-row');
    r.append(el('span', 'n', name));
    r.append(el('span', 'a', ` ${amount}`));
    r.append(el('span', 'p', pct != null ? `${pct}%` : '-'));
    box.append(r);
  }
  box.append(el('div', 'lc-thick'));
  box.append(el('div', 'lc-foot', R('nutrition_footnote')));
  return box;
}

// ---------- 화면에 축소해 넣기 / 1080px 이미지로 내보내기 ----------

/** 카드를 부모 너비에 맞게 축소해 넣는 틀. 크기가 바뀌면 fitCards() 로 다시 맞춘다. */
function cardFrame(card, templateName, formatName, showWatermark) {
  const frame = el('div', 'card-frame');
  frame.dataset.h = CARD_FORMATS[formatName].height;
  frame.append(cardElement(card, templateName, formatName, showWatermark));
  requestAnimationFrame(() => fitFrame(frame));
  return frame;
}

function fitFrame(frame) {
  const w = frame.clientWidth;
  if (!w) return;
  const s = w / CARD_WIDTH;
  frame.style.height = `${Number(frame.dataset.h) * s}px`;
  frame.firstChild.style.transform = `scale(${s})`;
}

function fitCards() { document.querySelectorAll('.card-frame').forEach(fitFrame); }
window.addEventListener('resize', fitCards);

/** 카드를 1080px 너비 PNG Blob 으로 만든다. */
async function exportCardPng(card, templateName, formatName, showWatermark) {
  await document.fonts.ready;
  const host = el('div', 'export-host');
  const node = cardElement(card, templateName, formatName, showWatermark);
  host.append(node);
  document.body.append(host);
  try {
    const canvas = await html2canvas(node, { scale: 1080 / CARD_WIDTH, backgroundColor: null, logging: false });
    return await new Promise((res) => canvas.toBlob(res, 'image/png'));
  } finally {
    host.remove();
  }
}
