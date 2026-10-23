// 웹 체험판. 실제 앱(안드로이드)의 화면 흐름을 예시 데이터로 재현한다.
// 사용 기록·걸음·수면은 웹에서 읽을 수 없으므로 모두 예시 값이고, 입력은 이 브라우저(localStorage)에만 남는다.
'use strict';

// ---------- 웹 전용 문구 ----------
const WEB = {
  ko: {
    bar: 'WEB DEMO · 실제 앱은 안드로이드',
    lab: '실험실',
    demoData: '체험판: 사용 기록·걸음·수면은 예시 데이터입니다.',
    permDemo: '체험판에서는 권한을 켤 수 없습니다. 실제 앱에서는 이 버튼이 시스템 설정 화면을 엽니다.',
    permState: '체험판',
    adBanner: '광고 · 배너 자리 (실제 앱: AdMob 적응형 배너)',
    adInterstitial: '전면 광고',
    adInterstitialNote: '실제 앱에서는 새 성분표를 처음 열 때 이 자리에 AdMob 전면 광고가 나옵니다. 하루 2회까지.',
    adRewarded: '보상형 광고',
    adRewardedNote: '체험판에서는 실제 광고 대신 잠시 기다립니다.',
    close: '닫기',
    claim: '보상 받기',
    wait: (s) => `${s}초 남음`,
    imageTitle: '카드 이미지',
    imageHint: '아이폰: 이미지를 길게 누르면 \'사진 앱에 저장\'이 나옵니다.',
    shareSheet: '공유 · 사진에 저장',
    download: '파일로 내려받기',
    making: '이미지를 만드는 중…',
    language: '언어 (체험판)',
    reset: '체험판 초기화',
    resetDone: '처음 상태로 돌렸습니다.',
    versionValue: '0.1.0 · 웹 체험판',
    licensesText: '앱에 포함된 글꼴: Pretendard (Kil Hyung-jin), JetBrains Mono (The JetBrains Mono Project Authors) — SIL Open Font License 1.1.\n\n웹 체험판 이미지 저장: html2canvas (Niklas von Hertzen) — MIT License.',
    labTitle: '실험실',
    labIntro: '원자료를 바꿔 가며 어떤 조건의 문구가 고르는지 확인합니다. 앱과 같은 문구 은행(phrases_ko.json)과 같은 규칙을 씁니다.',
    labScreen: '1일 화면 시간',
    labLate: '새벽(0~5시) 사용 비율',
    labSleep: '평균 수면',
    labNoSleep: '수면 측정 불가',
    labSteps: '1일 걸음',
    labUnlocks: '1일 잠금 해제',
    labTop: '1위 앱 비중',
    labMatched: '조건에 맞은 그룹',
    labNone: '(없음 → 기본 그룹)',
    labReroll: '문구 다시 뽑기',
    labBank: '문구 은행 전체 보기',
    bankTitle: '문구 은행',
    bankIntro: '카테고리 → 조건 그룹 → 문구. 우선순위가 높은 그룹부터 고릅니다. 수정은 app/src/main/assets/phrases_ko.json.',
    always: '항상 (기본)',
    priority: (p) => `우선순위 ${p}`,
    pickCount: (n) => `카드당 ${n}개`,
    catName: { caution: '주의사항', storage: '보관방법', expiry: '소비기한' },
    metricName: {
      lateNightRatio: '새벽 사용 비율', dailyScreenHours: '1일 화면 시간(시간)', dailySteps: '1일 걸음',
      avgSleepHours: '평균 수면(시간)', dailyUnlocks: '1일 잠금 해제', topAppShare: '1위 앱 비중',
    },
    permDone: '실제 앱에서는 여기서 권한을 켠 뒤 홈으로 갑니다.',
  },
  en: {
    bar: 'WEB DEMO · The real app is Android',
    lab: 'Lab',
    demoData: 'Demo: usage, steps and sleep are sample data.',
    permDemo: 'Permissions can\'t be granted in the web demo. In the real app this opens system settings.',
    permState: 'Demo',
    adBanner: 'Ad · banner slot (real app: AdMob adaptive banner)',
    adInterstitial: 'Interstitial ad',
    adInterstitialNote: 'In the real app an AdMob interstitial appears here the first time a new label is opened. Max 2 a day.',
    adRewarded: 'Rewarded ad',
    adRewardedNote: 'The demo waits briefly instead of showing a real ad.',
    close: 'Close',
    claim: 'Get reward',
    wait: (s) => `${s}s left`,
    imageTitle: 'Card image',
    imageHint: 'On iPhone, long-press the image and choose "Save to Photos".',
    shareSheet: 'Share · Save to Photos',
    download: 'Download file',
    making: 'Creating image…',
    language: 'Language (demo)',
    reset: 'Reset demo',
    resetDone: 'Demo reset.',
    versionValue: '0.1.0 · web demo',
    licensesText: 'Fonts bundled in the app: Pretendard (Kil Hyung-jin), JetBrains Mono (The JetBrains Mono Project Authors) — SIL Open Font License 1.1.\n\nImage export in the web demo: html2canvas (Niklas von Hertzen) — MIT License.',
    labTitle: 'Lab',
    labIntro: 'Change the raw data and see which conditional phrases get picked. Same phrase bank and rules as the app.',
    labScreen: 'Screen time / day',
    labLate: 'Late-night (0–5 a.m.) share',
    labSleep: 'Average sleep',
    labNoSleep: 'Sleep not measured',
    labSteps: 'Steps / day',
    labUnlocks: 'Unlocks / day',
    labTop: 'Top app share',
    labMatched: 'Matched groups',
    labNone: '(none → default group)',
    labReroll: 'New wording',
    labBank: 'Browse the phrase bank',
    bankTitle: 'Phrase bank',
    bankIntro: 'Category → condition group → phrases. Higher priority groups are picked first. Edit app/src/main/assets/phrases_en.json.',
    always: 'Always (default)',
    priority: (p) => `priority ${p}`,
    pickCount: (n) => `${n} per card`,
    catName: { caution: 'Warning', storage: 'Storage', expiry: 'Use by' },
    metricName: {
      lateNightRatio: 'late-night share', dailyScreenHours: 'screen hours/day', dailySteps: 'steps/day',
      avgSleepHours: 'avg sleep (h)', dailyUnlocks: 'unlocks/day', topAppShare: 'top app share',
    },
    permDone: 'In the real app you grant permissions here, then go Home.',
  },
};
const W = () => WEB[I18N.lang];

// ---------- 저장소 (이 브라우저에만) ----------
const Store = {
  get(key, fallback) {
    try {
      const v = localStorage.getItem(`nalabel.${key}`);
      return v == null ? fallback : JSON.parse(v);
    } catch { return fallback; }
  },
  set(key, value) {
    try { localStorage.setItem(`nalabel.${key}`, JSON.stringify(value)); } catch { /* 사파리 사생활 보호 모드 등 */ }
  },
  clear() {
    try { Object.keys(localStorage).filter((k) => k.startsWith('nalabel.')).forEach((k) => localStorage.removeItem(k)); } catch { /* 무시 */ }
  },
};

const BANKS = {};
const bank = () => BANKS[I18N.lang];

// ---------- 예시 데이터 ----------
const APPS = [
  ['com.google.android.youtube', 'YouTube', 'YouTube'],
  ['com.instagram.android', 'Instagram', 'Instagram'],
  ['com.kakao.talk', '카카오톡', 'KakaoTalk'],
  ['com.android.chrome', 'Chrome', 'Chrome'],
  ['com.nhn.android.webtoon', '네이버 웹툰', 'Naver Webtoon'],
  ['com.coupang.mobile', '쿠팡', 'Coupang'],
  ['viva.republica.toss', '토스', 'Toss'],
  ['com.netflix.mediaclient', 'Netflix', 'Netflix'],
];
const appLabel = (pkg) => {
  const a = APPS.find((x) => x[0] === pkg);
  return a ? (I18N.lang === 'ko' ? a[1] : a[2]) : pkg;
};

/** p: 하루 기준 값과 앱 비중. [앱 인덱스, 비중] 목록의 비중 합은 1. */
function makeStats(weekStart, days, p) {
  const screenMs = Math.round(p.screenH * HOUR * days);
  const apps = p.mix
    .map(([i, share]) => ({ pkg: APPS[i][0], label: appLabel(APPS[i][0]), ms: Math.round(screenMs * share) }))
    .sort((a, b) => b.ms - a.ms);
  return {
    weekStart, days, apps, screenMs,
    unlocks: Math.round(p.unlocks * days),
    steps: Math.round(p.steps * days),
    sleepTotalMs: p.sleepH != null ? Math.round(p.sleepH * HOUR * days) : 0,
    sleepNights: p.sleepH != null ? days : 0,
    lateNightMs: Math.round(screenMs * p.late),
  };
}

const PROFILES = [
  // 이번 주(진행 중)
  { screenH: 4.4, late: 0.17, sleepH: 6.3, steps: 6100, unlocks: 104, mix: [[0, 0.33], [1, 0.21], [2, 0.18], [4, 0.1], [3, 0.08], [5, 0.06], [6, 0.04]] },
  // 지난주: 새벽형, 유튜브 편중
  { screenH: 6.6, late: 0.28, sleepH: 5.4, steps: 4200, unlocks: 142, mix: [[0, 0.52], [1, 0.14], [2, 0.12], [7, 0.1], [3, 0.07], [5, 0.05]] },
  // 2주 전: 균형
  { screenH: 3.1, late: 0.05, sleepH: 7.2, steps: 8800, unlocks: 71, mix: [[2, 0.27], [0, 0.24], [1, 0.2], [3, 0.14], [6, 0.08], [5, 0.07]] },
  // 3주 전: 많이 걸음
  { screenH: 2.2, late: 0.04, sleepH: 7.8, steps: 13200, unlocks: 55, mix: [[2, 0.3], [3, 0.22], [1, 0.2], [0, 0.18], [6, 0.1]] },
  // 4주 전: 잠 많이, 잘 안 걸음
  { screenH: 4.0, late: 0.11, sleepH: 8.9, steps: 2600, unlocks: 96, mix: [[4, 0.36], [0, 0.25], [2, 0.17], [1, 0.12], [7, 0.1]] },
];

function currentWeekStart() { return Dates.weekStartOf(Dates.todayEpochDay()); }
function archiveWeeks() { return [1, 2, 3, 4].map((k) => currentWeekStart() - 7 * k); }
function statsFor(ws) {
  const cur = currentWeekStart();
  if (ws === cur) {
    const days = ((Dates.dow(Dates.todayEpochDay()) + 6) % 7) + 1;
    return makeStats(ws, days, PROFILES[0]);
  }
  const k = Math.round((cur - ws) / 7);
  return makeStats(ws, 7, PROFILES[Math.min(Math.max(k, 1), PROFILES.length - 1)]);
}

// ---------- 문구 고르기 (data/PhraseEngine.kt) ----------
const Recent = {
  get(cat) { return new Set(Store.get(`recent.${cat}`, [])); },
  add(cat, ids) {
    const cur = Store.get(`recent.${cat}`, []).filter((x) => !ids.includes(x));
    Store.set(`recent.${cat}`, [...cur, ...ids].slice(-30));
  },
};

function pickPhraseIds(stats, rnd, { record = false, recent = true } = {}) {
  const m = PhraseMetrics.from(stats);
  const pick = (cat) => {
    const r = PhraseSelector.select(bank(), cat, m, recent ? Recent.get(cat) : new Set(), rnd).map((p) => p.id);
    if (record) Recent.add(cat, r);
    return r;
  };
  return { cautions: pick('caution'), storage: pick('storage')[0] ?? null, expiry: pick('expiry')[0] ?? null };
}

function rerollIds(stats, ids, cat) {
  const exclude = new Set([...ids.cautions, ids.storage, ids.expiry].filter(Boolean));
  const picked = PhraseSelector.select(bank(), cat, PhraseMetrics.from(stats), Recent.get(cat), Math.random, exclude).map((p) => p.id);
  if (!picked.length) return ids;
  Recent.add(cat, picked);
  if (cat === 'caution') return { ...ids, cautions: picked };
  if (cat === 'storage') return { ...ids, storage: picked[0] };
  return { ...ids, expiry: picked[0] };
}

function phrasesFromIds(ids) {
  const p = (id) => (id ? { id, text: phraseText(bank(), id) ?? '' } : null);
  return { cautions: ids.cautions.map(p), storage: p(ids.storage), expiry: p(ids.expiry) };
}

// ---------- 카드 상태 (data/CardRepository.kt) ----------
function productName() { return Store.get('nickname', '') || R('default_nickname'); }
function hiddenPackages() { return new Set(Store.get('hiddenPackages', [])); }

/** 저장된 카드(지난주들) 또는 진행 중인 이번 주. 편집 상태는 지난 카드만 저장한다. */
function storedCard(ws) {
  const live = ws === 'live';
  const week = live ? currentWeekStart() : ws;
  const stats = statsFor(week);
  const saved = live ? null : Store.get(`card.${week}`, null);
  return {
    week, live, stats,
    ids: saved?.ids ?? pickPhraseIds(stats, seededRandom(week * 31 + 7), { recent: false }),
    hidden: new Set(saved?.hidden ?? []),
    template: saved?.template ?? 'CLASSIC',
  };
}

function saveCard(c) {
  if (c.live) return;
  Store.set(`card.${c.week}`, { ids: c.ids, hidden: [...c.hidden], template: c.template });
}

function toLabel(c) {
  return LabelBuilder.build(c.stats, productName(), phrasesFromIds(c.ids), hiddenPackages(), c.hidden, bank().fallbackTopApp);
}

const isRevealed = (week) => Store.get('revealed', []).includes(week);
const markRevealed = (week) => { if (!isRevealed(week)) Store.set('revealed', [...Store.get('revealed', []), week]); };

// ---------- 공통 UI ----------
function h(tag, attrs = {}, ...children) {
  const e = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs || {})) {
    if (v == null || v === false) continue;
    if (k === 'class') e.className = v;
    else if (k.startsWith('on')) e.addEventListener(k.slice(2), v);
    else if (k === 'html') e.innerHTML = v;
    else e.setAttribute(k, v === true ? '' : v);
  }
  for (const c of children.flat()) if (c != null && c !== false) e.append(c instanceof Node ? c : String(c));
  return e;
}

function toast(msg) {
  document.querySelectorAll('.toast').forEach((t) => t.remove());
  const t = h('div', { class: 'toast' }, msg);
  document.body.append(t);
  setTimeout(() => t.remove(), 2600);
}

function overlay(content, { onClose, dismissable = true } = {}) {
  const o = h('div', { class: 'overlay' }, content);
  const close = () => { o.remove(); onClose?.(); };
  if (dismissable) o.addEventListener('click', (ev) => { if (ev.target === o) close(); });
  document.body.append(o);
  return close;
}

function adSlot() { return h('div', { class: 'ad-slot' }, W().adBanner); }

function cardBox(card, template, format, watermark, onClick) {
  const frame = cardFrame(card, template, format, watermark);
  if (onClick) { frame.classList.add('clickable'); frame.addEventListener('click', onClick); }
  return h('div', { class: 'card-box' }, frame);
}

// ---------- 광고 흉내 (ads/AdsManager.kt 규칙) ----------
function showInterstitialThen(onDone) {
  const today = Dates.todayEpochDay();
  const log = Store.get('interstitial', { day: -1, count: 0 });
  const count = log.day === today ? log.count : 0;
  if (count >= 2) { onDone(); return; }
  Store.set('interstitial', { day: today, count: count + 1 });
  const btn = h('button', { class: 'btn', disabled: true }, W().close);
  const close = overlay(h('div', { class: 'ad-sim' },
    h('div', { class: 'mono' }, 'AD'),
    h('div', { class: 'big' }, W().adInterstitial),
    h('p', { class: 'small' }, W().adInterstitialNote),
    btn), { dismissable: false, onClose: onDone });
  setTimeout(() => { btn.disabled = false; }, 1500);
  btn.addEventListener('click', close);
}

function showRewarded(onRewarded) {
  let left = 4;
  const status = h('div', { class: 'mono' }, W().wait(left));
  const claim = h('button', { class: 'btn', disabled: true }, W().claim);
  let rewarded = false;
  const close = overlay(h('div', { class: 'ad-sim' },
    h('div', { class: 'mono' }, 'AD'),
    h('div', { class: 'big' }, W().adRewarded),
    h('p', { class: 'small' }, W().adRewardedNote),
    status, claim,
    h('button', { class: 'btn-text', onclick: () => close() }, W().close)), { dismissable: false });
  const timer = setInterval(() => {
    left -= 1;
    status.textContent = left > 0 ? W().wait(left) : '';
    if (left <= 0) { clearInterval(timer); claim.disabled = false; }
  }, 1000);
  claim.addEventListener('click', () => {
    if (rewarded) return;
    rewarded = true;
    clearInterval(timer);
    close();
    onRewarded();
  });
}

// ---------- 이미지 저장/공유 (ui/card/CardExportBar.kt) ----------
function exportBar(getArgs, onExported) {
  async function run() {
    const busy = overlay(h('div', { class: 'dialog' }, h('p', {}, W().making)), { dismissable: false });
    let blob;
    try {
      blob = await exportCardPng(...getArgs());
    } catch (e) {
      busy();
      toast(R('export_failed'));
      return;
    }
    busy();
    onExported?.();
    const [card, , format] = getArgs();
    const name = `nalabel_${CardText.lot(card.periodStart).replace('LOT ', '')}_${format.toLowerCase()}.png`;
    const file = new File([blob], name, { type: 'image/png' });
    const url = URL.createObjectURL(blob);
    const canShare = !!(navigator.canShare && navigator.canShare({ files: [file] }));
    const close = overlay(h('div', { class: 'dialog' },
      h('h2', {}, W().imageTitle),
      h('img', { src: url, alt: '' }),
      h('p', { class: 'small muted' }, `${W().imageHint}  ·  1080 × ${format === 'STORY' ? 1920 : 1080}`),
      canShare && h('button', {
        class: 'btn',
        onclick: async () => {
          try { await navigator.share({ files: [file] }); } catch { /* 취소 */ }
        },
      }, W().shareSheet),
      h('a', { class: 'btn-outline center', href: url, download: name, style: 'text-decoration:none' }, W().download),
      h('button', { class: 'btn-text', onclick: () => close() }, W().close),
    ), { onClose: () => URL.revokeObjectURL(url) });
  }
  return h('div', { class: 'btn-row' },
    h('button', { class: 'btn-outline center', onclick: run }, R('action_save')),
    h('button', { class: 'btn', onclick: run }, R('action_share')));
}

// ---------- 화면들 ----------
const Screens = {};

// 온보딩 (ui/onboarding/OnboardingScreen.kt)
const onboarding = { stage: 'intro', answers: [] };
Screens.onboarding = () => {
  const s = onboarding;
  const page = (title, body, action, next) => h('div', { class: 'screen', style: 'min-height:calc(100dvh - 40px)' },
    h('div', { class: 'spacer' }),
    h('h1', {}, title), h('div', { class: 'hr' }), h('p', {}, body),
    h('div', { class: 'spacer', style: 'flex:1.4' }),
    h('button', { class: 'btn', onclick: next }, action));

  if (s.stage === 'intro') {
    return page(R('onboarding_intro_title'), R('onboarding_intro_body'), R('onboarding_next'), () => { s.stage = 'privacy'; render(); });
  }
  if (s.stage === 'privacy') {
    return page(R('onboarding_privacy_title'), R('onboarding_privacy_body'), R('onboarding_try_quiz'), () => { s.stage = 'quiz'; s.answers = []; render(); });
  }
  if (s.stage === 'quiz') {
    const i = s.answers.length;
    const options = I18N.arr(`quiz_q${i + 1}_options`);
    return h('div', { class: 'screen' },
      h('div', { class: 'muted', style: 'font-weight:600' }, R('quiz_progress', i + 1, 5)),
      h('h1', {}, R(`quiz_q${i + 1}`)), h('div', { class: 'hr' }),
      options.map((o, idx) => h('button', {
        class: 'btn-outline',
        onclick: () => { s.answers.push(idx); if (s.answers.length === 5) s.stage = 'result'; render(); },
      }, o)),
      i > 0 && h('button', { class: 'btn-text', onclick: () => { s.answers.pop(); render(); } }, '←'));
  }
  // 결과: domain/Quiz.kt 와 같은 환산
  const a = s.answers;
  const labels = I18N.arr('quiz_app_labels');
  const ws = currentWeekStart();
  const days = 7;
  const screenH = [1.5, 3.0, 5.0, 7.5][a[0]];
  const screenMs = screenH * HOUR * days;
  const top = a[1];
  const others = [0, 1, 2, 3, 4].filter((x) => x !== top).slice(0, 2);
  const shares = [0.46, 0.22, 0.13];
  const stats = {
    weekStart: ws, days,
    apps: [top, ...others].map((c, i) => ({ pkg: `quiz.${c}`, label: labels[c], ms: Math.round(screenMs * shares[i]) })),
    screenMs,
    unlocks: [28, 85, 160][a[4]] * days,
    steps: [2400, 6500, 11800][a[3]] * days,
    sleepTotalMs: [7.4, 6.4, 5.1][a[2]] * HOUR * days,
    sleepNights: days,
    lateNightMs: screenMs * [0.03, 0.12, 0.32][a[2]],
  };
  const seed = a.reduce((acc, x) => acc * 31 + x, 17);
  const ids = pickPhraseIds(stats, seededRandom(seed), { recent: false });
  const card = LabelBuilder.build(stats, productName(), phrasesFromIds(ids), new Set(), new Set(), bank().fallbackTopApp);
  const finish = (to) => { Store.set('onboardingDone', true); onboarding.stage = 'intro'; location.hash = to; };
  return h('div', { class: 'screen' },
    h('h2', {}, R('quiz_result_title')),
    cardBox(card, 'CLASSIC', 'STORY', true),
    exportBar(() => [card, 'CLASSIC', 'STORY', true]),
    h('div', { class: 'hr-soft' }),
    h('p', {}, R('quiz_result_real_data_hint')),
    h('button', { class: 'btn', onclick: () => finish('#/permissions?onboarding=1') }, R('quiz_result_real_data')),
    h('button', { class: 'btn-text', onclick: () => finish('#/home') }, R('quiz_result_later')));
};

// 권한 (ui/permissions/PermissionsScreen.kt)
Screens.permissions = (params) => {
  const item = (title, desc) => h('div', { class: 'ruled' },
    h('div', { style: 'display:flex;align-items:center' }, h('h2', { style: 'flex:1' }, title), h('span', { class: 'muted', style: 'font-weight:600' }, W().permState)),
    h('p', { class: 'small muted' }, desc),
    h('button', { class: 'btn-outline center', onclick: () => toast(W().permDemo) }, R('permission_open_settings')));
  return h('div', { class: 'screen' },
    h('h1', {}, R('permissions_title')),
    h('p', {}, R('permissions_intro')),
    item(R('permission_usage_title'), R('permission_usage_desc')),
    item(R('permission_activity_title'), R('permission_activity_desc')),
    item(R('permission_notification_title'), R('permission_notification_desc')),
    params.get('onboarding') && h('p', { class: 'small muted' }, W().permDone),
    h('button', { class: 'btn', onclick: () => { if (params.get('onboarding')) location.hash = '#/home'; else history.back(); } }, R('permissions_done')));
};

function openPublished(week) {
  const go = () => { location.hash = `#/result/${week}`; };
  if (!isRevealed(week)) showInterstitialThen(go); else go();
}

// 홈 (ui/screens/HomeScreen.kt)
Screens.home = () => {
  const live = storedCard('live');
  const latest = archiveWeeks()[0];
  const next = formatPattern(Dates.nextPublishAt(), R('fmt_publish_datetime'));
  return h('div', {},
    h('div', { class: 'screen' },
      h('h1', {}, R('home_title')),
      h('p', { class: 'muted' }, R('home_next_publish', next)),
      h('div', { class: 'hr' }),
      !isRevealed(latest) && h('div', { class: 'ruled' },
        h('h2', {}, R('home_new_label_title')),
        h('p', { class: 'small muted' }, `${CardText.lot(latest)}  ${CardText.dateRange(latest, latest + 6)}`),
        h('button', { class: 'btn', onclick: () => openPublished(latest) }, R('home_new_label_open'))),
      cardBox(toLabel(live), 'CLASSIC', 'SQUARE', true, () => { location.hash = '#/result/live'; }),
      h('p', { class: 'small muted' }, R('home_live_caption')),
      h('p', { class: 'small muted' }, W().demoData),
      h('button', { class: 'btn', onclick: () => { location.hash = '#/result/live'; } }, R('home_open_card'))));
};

// 결과 카드 (ui/result/ResultScreen.kt). 광고 없음.
const resultView = { format: 'STORY', week: null, removeWatermark: false, paid: false };
/** 진행 중인 주의 편집 상태. 저장하지 않고 결과 화면을 떠나면 버린다(앱과 같음). */
let liveEdits = null;
Screens.result = (params, arg) => {
  let c = storedCard(arg === 'live' ? 'live' : Number(arg));
  if (c.live && liveEdits) c = liveEdits;
  if (!c.live) markRevealed(c.week);
  if (resultView.week !== arg) {
    // 다른 카드로 오면 워터마크 제거 상태와 '이 카드에서 1회 사용'을 초기화
    Object.assign(resultView, { week: arg, removeWatermark: false, paid: false });
  }
  const credits = Store.get('watermarkCredits', 0);
  const unlocked = new Set(Store.get('unlockedTemplates', []));
  const card = toLabel(c);
  const update = (mutate) => {
    mutate(c);
    if (c.live) liveEdits = c; else saveCard(c);
    render();
  };

  const templateChips = Object.entries(CARD_TEMPLATES).map(([name, t]) => {
    const available = t.free || unlocked.has(name);
    return h('button', {
      class: `chip ${c.template === name ? 'on' : ''}`,
      onclick: () => {
        if (available) { update((x) => { x.template = name; }); return; }
        const close = overlay(h('div', { class: 'dialog' },
          h('h2', {}, R('reward_unlock_title', R(t.label))),
          h('p', {}, R('reward_unlock_text')),
          h('div', { class: 'btn-row' },
            h('button', { class: 'btn-outline center', onclick: () => close() }, R('action_cancel')),
            h('button', {
              class: 'btn',
              onclick: () => {
                close();
                showRewarded(() => {
                  Store.set('unlockedTemplates', [...unlocked, name]);
                  update((x) => { x.template = name; });
                });
              },
            }, R('reward_watch')))));
      },
    }, available ? R(t.label) : R('template_locked', R(t.label)));
  });

  const watermarkSwitch = h('input', { type: 'checkbox', class: 'switch', checked: resultView.removeWatermark });
  watermarkSwitch.addEventListener('change', () => {
    if (!watermarkSwitch.checked) { resultView.removeWatermark = false; render(); return; }
    if (credits > 0 || resultView.paid) { resultView.removeWatermark = true; render(); return; }
    watermarkSwitch.checked = false;
    const close = overlay(h('div', { class: 'dialog' },
      h('h2', {}, R('watermark_remove')),
      h('p', {}, R('reward_watermark_text')),
      h('div', { class: 'btn-row' },
        h('button', { class: 'btn-outline center', onclick: () => close() }, R('action_cancel')),
        h('button', {
          class: 'btn',
          onclick: () => {
            close();
            showRewarded(() => {
              Store.set('watermarkCredits', Store.get('watermarkCredits', 0) + 1);
              resultView.removeWatermark = true;
              render();
            });
          },
        }, R('reward_watch')))));
  });

  // 워터마크 없이 처음 내보낼 때만 1회 차감. 같은 카드는 저장·공유를 여러 번 해도 더 차감하지 않는다.
  const onExported = () => {
    if (!resultView.removeWatermark || resultView.paid) return;
    Store.set('watermarkCredits', Math.max(0, Store.get('watermarkCredits', 0) - 1));
    resultView.paid = true;
    render();
  };

  const editItems = [
    ...card.ingredients.map((i) => [i.key, R('result_edit_ingredient', CardText.ingredientName(i)), null]),
    [ItemKeys.SCREEN, R('nutrition_screen_time'), null],
    [ItemKeys.UNLOCKS, R('nutrition_unlocks'), null],
    [ItemKeys.STEPS, R('nutrition_steps'), null],
    [ItemKeys.SLEEP, R('nutrition_sleep'), null],
    [ItemKeys.STORAGE, R('card_row_storage'), 'storage'],
    [ItemKeys.CAUTION, R('card_row_caution'), 'caution'],
    [ItemKeys.EXPIRY, R('card_row_expiry'), 'expiry'],
  ];

  return h('div', { class: 'screen' },
    c.live && h('p', { class: 'small muted' }, R('result_live_notice')),
    cardBox(card, c.template, resultView.format, !resultView.removeWatermark),
    exportBar(() => [card, c.template, resultView.format, !resultView.removeWatermark], onExported),
    h('div', { class: 'switch-row' },
      h('div', { class: 't' }, h('div', {}, R('watermark_remove')), h('div', { class: 'small muted' }, R('watermark_credits', credits))),
      watermarkSwitch),
    h('div', { class: 'chips' }, Object.entries(CARD_FORMATS).map(([name, f]) => h('button', {
      class: `chip ${resultView.format === name ? 'on' : ''}`,
      onclick: () => { resultView.format = name; render(); },
    }, R(f.label)))),
    h('div', { class: 'chips' }, templateChips),
    h('div', { class: 'section-title' }, R('result_edit_title')),
    h('div', {}, editItems.map(([key, label, cat]) => {
      const visible = !c.hidden.has(key);
      const box = h('input', { type: 'checkbox', checked: visible });
      box.addEventListener('change', () => update((x) => { if (box.checked) x.hidden.delete(key); else x.hidden.add(key); }));
      return h('label', { class: 'check-row' }, box, h('span', { class: 't' }, label),
        cat && h('button', {
          disabled: !visible,
          onclick: (ev) => { ev.preventDefault(); update((x) => { x.ids = rerollIds(x.stats, x.ids, cat); }); },
        }, R('result_reroll')));
    }),
    h('button', { class: 'btn-text', disabled: !c.hidden.size, onclick: () => update((x) => { x.hidden.clear(); }) }, R('result_edit_show_all'))));
};

// 보관함 (ui/archive/ArchiveScreen.kt)
Screens.archive = () => h('div', { class: 'screen' },
  h('h1', {}, R('archive_title')), h('div', { class: 'hr' }),
  h('div', { class: 'grid' }, archiveWeeks().map((ws) => {
    const c = storedCard(ws);
    return h('div', { class: 'item', onclick: () => openPublished(ws) },
      cardBox(toLabel(c), c.template, 'SQUARE', false),
      h('div', { class: 'mono' }, CardText.lot(ws)),
      h('div', { class: 'small muted' }, CardText.dateRange(ws, ws + 6)));
  })));

// 설정 (ui/settings/*)
Screens.settings = () => {
  const row = (title, desc, value, onclick) => h('div', { class: 'label-row', onclick },
    h('div', { class: 't' }, h('div', {}, title), desc && h('div', { class: 'small muted' }, desc)),
    value != null && h('div', { class: 'v' }, value));
  const editName = () => {
    const input = h('input', { type: 'text', value: Store.get('nickname', ''), placeholder: R('default_nickname'), maxlength: 12 });
    const close = overlay(h('div', { class: 'dialog' },
      h('h2', {}, R('settings_nickname')), input,
      h('div', { class: 'btn-row' },
        h('button', { class: 'btn-outline center', onclick: () => close() }, R('action_cancel')),
        h('button', { class: 'btn', onclick: () => { Store.set('nickname', input.value.trim().slice(0, 12)); close(); render(); } }, R('action_ok')))));
    input.focus();
  };
  return h('div', {},
    h('div', { class: 'screen', style: 'gap:4px' },
      h('h1', {}, R('settings_title')),
      h('div', { class: 'section-title' }, R('settings_section_card')),
      row(R('settings_nickname'), R('settings_nickname_desc'), productName(), editName),
      row(R('settings_hidden_apps'), R('settings_hidden_apps_desc'), R('settings_hidden_apps_count', hiddenPackages().size), () => { location.hash = '#/settings/hidden'; }),
      h('div', { class: 'section-title' }, R('settings_section_data')),
      row(R('settings_permissions'), R('settings_permissions_desc'), null, () => { location.hash = '#/permissions'; }),
      row(R('settings_privacy'), null, '↗', () => { location.href = 'privacy-policy.html'; }),
      h('div', { class: 'section-title' }, R('settings_section_info')),
      row(R('settings_licenses'), null, null, () => { location.hash = '#/licenses'; }),
      row(R('settings_version'), null, W().versionValue),
      row(W().language, null, I18N.lang === 'ko' ? '한국어' : 'English', () => setLang(I18N.lang === 'ko' ? 'en' : 'ko')),
      row(W().reset, null, null, () => { Store.clear(); liveEdits = null; toast(W().resetDone); location.hash = '#/onboarding'; render(); })));
};

Screens.hidden = () => {
  const hidden = hiddenPackages();
  return h('div', { class: 'screen' },
    h('h1', {}, R('settings_hidden_apps')),
    h('p', { class: 'small muted' }, R('hidden_apps_intro')),
    h('div', {}, [...APPS].sort((a, b) => appLabel(a[0]).localeCompare(appLabel(b[0]))).map(([pkg]) => {
      const box = h('input', { type: 'checkbox', checked: hidden.has(pkg) });
      box.addEventListener('change', () => {
        if (box.checked) hidden.add(pkg); else hidden.delete(pkg);
        Store.set('hiddenPackages', [...hidden]);
      });
      return h('label', { class: 'check-row' }, box, h('span', { class: 't' }, appLabel(pkg)));
    })));
};

Screens.licenses = () => h('div', { class: 'screen' },
  h('h1', {}, R('settings_licenses')),
  h('p', { class: 'small' }, W().licensesText));

// 실험실: 원자료 → 문구 선택 확인 (웹 체험판 전용)
const lab = { screenH: 5, late: 22, sleepH: 6.2, noSleep: false, steps: 5200, unlocks: 110, top: 48, seed: 1 };
Screens.lab = () => {
  const make = () => {
    const rest = (100 - lab.top) / 100;
    const p = {
      screenH: lab.screenH, late: lab.late / 100, sleepH: lab.noSleep ? null : lab.sleepH,
      steps: lab.steps, unlocks: lab.unlocks,
      mix: [[0, lab.top / 100], [1, rest * 0.4], [2, rest * 0.35], [3, rest * 0.25]],
    };
    return makeStats(currentWeekStart(), 7, p);
  };
  const stats = make();
  const ids = pickPhraseIds(stats, seededRandom(lab.seed), { recent: false });
  const card = LabelBuilder.build(stats, productName(), phrasesFromIds(ids), hiddenPackages(), new Set(), bank().fallbackTopApp);
  const metrics = PhraseMetrics.from(stats);

  const slider = (key, label, min, max, step, fmt) => {
    const value = h('span', { class: 'mono' }, fmt(lab[key]));
    const input = h('input', { type: 'range', min, max, step, value: lab[key] });
    input.addEventListener('input', () => { lab[key] = Number(input.value); value.textContent = fmt(lab[key]); });
    input.addEventListener('change', () => render());
    return h('label', {}, h('div', { class: 'head' }, h('span', {}, label), value), input);
  };
  const noSleep = h('input', { type: 'checkbox', checked: lab.noSleep });
  noSleep.addEventListener('change', () => { lab.noSleep = noSleep.checked; render(); });

  const condText = (c) => `${W().metricName[c.metric] ?? c.metric} ${c.op} ${c.value}`;
  return h('div', { class: 'screen' },
    h('h1', {}, W().labTitle),
    h('p', { class: 'small muted' }, W().labIntro),
    h('div', { class: 'lab-sliders' },
      slider('screenH', W().labScreen, 0.5, 12, 0.5, (v) => `${v}h`),
      slider('late', W().labLate, 0, 60, 1, (v) => `${v}%`),
      slider('sleepH', W().labSleep, 3, 11, 0.1, (v) => `${v.toFixed(1)}h`),
      h('label', { class: 'check-row', style: 'flex-direction:row' }, noSleep, h('span', { class: 't' }, W().labNoSleep)),
      slider('steps', W().labSteps, 0, 20000, 100, (v) => CardText.count(v)),
      slider('unlocks', W().labUnlocks, 5, 250, 5, (v) => String(v)),
      slider('top', W().labTop, 10, 90, 1, (v) => `${v}%`)),
    h('div', { class: 'section-title' }, W().labMatched),
    ['caution', 'storage', 'expiry'].map((cat) => {
      const groups = PhraseSelector.matchedGroups(bank(), cat, metrics);
      return h('div', { class: 'small' }, h('b', {}, `${W().catName[cat]}: `),
        groups.length ? groups.map((g) => h('span', { class: 'tag' }, `${g.id} · ${g.when.map(condText).join(', ')}`)) : W().labNone);
    }),
    cardBox(card, 'CLASSIC', 'STORY', true),
    h('div', { class: 'btn-row' },
      h('button', { class: 'btn-outline center', onclick: () => { lab.seed += 1; render(); } }, W().labReroll),
      h('button', { class: 'btn', onclick: () => { location.hash = '#/lab/bank'; } }, W().labBank)));
};

Screens.bank = () => {
  const b = bank();
  const condText = (c) => `${W().metricName[c.metric] ?? c.metric} ${c.op} ${c.value}`;
  return h('div', { class: 'screen' },
    h('h1', {}, W().bankTitle),
    h('p', { class: 'small muted' }, W().bankIntro),
    Object.entries(b.categories).map(([cat, def]) => h('div', {},
      h('div', { class: 'section-title' }, `${W().catName[cat] ?? cat} · ${W().pickCount(def.pick)}`),
      def.groups.map((g) => h('div', { class: 'bank-group small' },
        h('div', {}, h('b', {}, g.id), ' ', h('span', { class: 'tag' }, W().priority(g.priority)),
          g.when.length ? g.when.map((c) => h('span', { class: 'tag' }, condText(c))) : h('span', { class: 'tag' }, W().always)),
        h('ol', { start: 0 }, g.phrases.map((p) => h('li', {}, p))))))));
};

// ---------- 라우팅 ----------
const TABS = [['home', 'tab_home'], ['archive', 'tab_archive'], ['settings', 'tab_settings']];

function setLang(lang) {
  I18N.lang = lang;
  Store.set('lang', lang);
  document.documentElement.lang = lang;
  document.title = R('app_name');
  liveEdits = null;
  render();
}

function render() {
  const hash = location.hash || '';
  const [path, query] = hash.replace(/^#\/?/, '').split('?');
  const params = new URLSearchParams(query || '');
  const parts = path.split('/').filter(Boolean);
  let route = parts[0] || (Store.get('onboardingDone', false) ? 'home' : 'onboarding');
  let arg = parts[1];
  if (route === 'settings' && arg === 'hidden') route = 'hidden';
  if (route === 'lab' && arg === 'bank') route = 'bank';
  if (route !== 'result') liveEdits = null;
  const screen = Screens[route] || Screens.home;

  const app = document.getElementById('app');
  const bar = h('div', { class: 'demo-bar' }, h('span', {}, W().bar), h('a', { href: '#/lab' }, W().lab));
  const main = h('main', {}, screen(params, arg));
  // 배너는 홈·설정에서만, 앱처럼 탭 바로 위에 고정한다.
  const tabs = TABS.some(([r]) => r === route) && h('div', { class: 'footer' },
    (route === 'home' || route === 'settings') && adSlot(),
    h('nav', { class: 'tabbar' },
      TABS.map(([r, label]) => h('a', { href: `#/${r}`, class: r === route ? 'on' : '' }, R(label)))));
  const prevRoute = app.dataset.route;
  app.replaceChildren(bar, main, ...(tabs ? [tabs] : []));
  app.dataset.route = `${route}/${arg ?? ''}`;
  if (prevRoute !== app.dataset.route) window.scrollTo(0, 0);
  requestAnimationFrame(fitCards);
}

async function start() {
  const urlLang = new URLSearchParams(location.search).get('lang');
  I18N.lang = urlLang === 'en' || urlLang === 'ko' ? urlLang : Store.get('lang', 'ko');
  document.documentElement.lang = I18N.lang;
  document.title = R('app_name');
  const [ko, en] = await Promise.all(['ko', 'en'].map((l) => fetch(`demo/phrases_${l}.json`).then((r) => r.json())));
  BANKS.ko = ko;
  BANKS.en = en;
  await document.fonts.ready;
  window.addEventListener('hashchange', render);
  render();
}

start();
