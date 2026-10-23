// 앱(domain/, ui/card/CardText.kt)의 로직을 웹으로 옮긴 것. 규칙을 바꾸면 양쪽을 같이 고친다.
'use strict';

const HOUR = 3600000;
const MINUTE = 60000;
const DAY_MS = 86400000;

// ---------- 리소스 (strings.xml 에서 생성된 window.RES) ----------

const I18N = {
  lang: 'ko',
  str(name, ...args) {
    const s = (RES[this.lang].strings[name] ?? RES.ko.strings[name]);
    if (s == null) return `!${name}`;
    return s.replace(/%(\d)\$[sd]/g, (_, i) => String(args[Number(i) - 1]));
  },
  arr(name) {
    return RES[this.lang].arrays[name] ?? RES.ko.arrays[name] ?? [];
  },
};
const R = (name, ...args) => I18N.str(name, ...args);

// ---------- 날짜: epochDay(정수, 1970-01-01 기준 일수) ----------

const Dates = {
  todayEpochDay() {
    const n = new Date();
    return Math.round(Date.UTC(n.getFullYear(), n.getMonth(), n.getDate()) / DAY_MS);
  },
  toDate(epochDay) { return new Date(epochDay * DAY_MS); }, // UTC 자정, UTC getter 로만 읽는다
  dow(epochDay) { return this.toDate(epochDay).getUTCDay(); }, // 0=일
  weekStartOf(epochDay) { return epochDay - ((this.dow(epochDay) + 6) % 7); },
  /** 다음 일요일 20:00 (로컬). */
  nextPublishAt(now = new Date()) {
    const d = new Date(now);
    d.setHours(20, 0, 0, 0);
    d.setDate(d.getDate() + ((7 - d.getDay()) % 7));
    if (d <= now) d.setDate(d.getDate() + 7);
    return d;
  },
};

/** Java DateTimeFormatter 패턴 중 앱이 쓰는 토큰만 지원: yyyy MMM MM M dd d EEE E a h mm */
function formatPattern(date, pattern, { utc = false } = {}) {
  const lang = I18N.lang;
  const g = (f) => (utc ? date[`getUTC${f}`]() : date[`get${f}`]());
  const y = g('FullYear'), mo = g('Month'), d = g('Date'), hh = g('Hours'), mi = g('Minutes');
  const tz = utc ? { timeZone: 'UTC' } : {};
  const monthShort = new Intl.DateTimeFormat(lang, { month: 'short', ...tz }).format(date);
  const weekdayShort = new Intl.DateTimeFormat(lang, { weekday: 'short', ...tz }).format(date);
  const ampm = lang === 'ko' ? (hh < 12 ? '오전' : '오후') : (hh < 12 ? 'AM' : 'PM');
  const h12 = hh % 12 === 0 ? 12 : hh % 12;
  const tokens = {
    yyyy: String(y), MMM: monthShort, MM: String(mo + 1).padStart(2, '0'), M: String(mo + 1),
    dd: String(d).padStart(2, '0'), d: String(d), EEE: weekdayShort, E: weekdayShort.replace(/요일$/, ''),
    a: ampm, h: String(h12), mm: String(mi).padStart(2, '0'),
  };
  return pattern.replace(/yyyy|MMM|MM|M|dd|d|EEE|E|a|h|mm/g, (t) => tokens[t]);
}

// ---------- 카드 표기 (CardText.kt) ----------

const CardText = {
  duration(ms) {
    const total = Math.floor(ms / MINUTE);
    const h = Math.floor(total / 60), m = total % 60;
    if (total === 0 && ms > 0) return R('fmt_under_minute');
    if (h === 0) return R('fmt_minutes', m);
    if (m === 0) return R('fmt_hours', h);
    return R('fmt_hours_minutes', h, m);
  },
  count(v) { return Math.round(v).toLocaleString(I18N.lang === 'ko' ? 'ko-KR' : 'en-US'); },
  percent(v) { return v < 1 ? `${v.toFixed(1)}%` : `${Math.round(v)}%`; },
  ingredientName(ing) {
    if (ing.kind === 'SLEEP') return R('ingredient_sleep');
    if (ing.kind === 'WALK') return R('ingredient_walk');
    return ing.name;
  },
  ingredients(list) {
    return list.map((i) => `${this.ingredientName(i)} ${this.percent(i.percent)}`).join(R('list_separator'));
  },
  dateRange(start, end) {
    const p = R('fmt_date_pattern');
    return `${formatPattern(Dates.toDate(start), p, { utc: true })} ~ ${formatPattern(Dates.toDate(end), p, { utc: true })}`;
  },
  /** ISO 주차. 예) LOT 2026-W43 */
  lot(weekStart) {
    const thu = Dates.toDate(weekStart + 3);
    const year = thu.getUTCFullYear();
    const dayOfYear = Math.round((thu - Date.UTC(year, 0, 1)) / DAY_MS) + 1;
    const week = Math.floor((dayOfYear - 1) / 7) + 1;
    return `LOT ${year}-W${String(week).padStart(2, '0')}`;
  },
  dailyValuePercent(total, dailyRef, days) {
    if (days <= 0 || dailyRef <= 0) return 0;
    return Math.round((total / (dailyRef * days)) * 100);
  },
};

const DailyReference = { SCREEN_MS: 3 * HOUR, UNLOCKS: 60, STEPS: 10000, SLEEP_MS: 8 * HOUR };

const ItemKeys = {
  SCREEN: 'nut:screen', UNLOCKS: 'nut:unlocks', STEPS: 'nut:steps', SLEEP: 'nut:sleep',
  CAUTION: 'phrase:caution', STORAGE: 'phrase:storage', EXPIRY: 'phrase:expiry',
  INGREDIENT_SLEEP: 'ing:sleep', INGREDIENT_WALK: 'ing:walk',
  app: (pkg) => `ing:app:${pkg}`,
};

// ---------- 카드 만들기 (LabelBuilder.kt) ----------

const LabelBuilder = {
  MAX_APPS: 4,
  MS_PER_STEP: 600,

  avgSleepMs(stats) { return stats.sleepNights > 0 ? stats.sleepTotalMs / stats.sleepNights : null; },

  ingredients(stats, hiddenPackages) {
    const walkMs = stats.steps * this.MS_PER_STEP;
    const pool = stats.screenMs + stats.sleepTotalMs + walkMs;
    if (pool <= 0) return [];
    const items = stats.apps
      .filter((a) => !hiddenPackages.has(a.pkg) && a.ms > 0)
      .slice(0, this.MAX_APPS)
      .map((a) => ({ key: ItemKeys.app(a.pkg), kind: 'APP', name: a.label, percent: (a.ms / pool) * 100 }));
    if (stats.sleepTotalMs > 0) items.push({ key: ItemKeys.INGREDIENT_SLEEP, kind: 'SLEEP', name: '', percent: (stats.sleepTotalMs / pool) * 100 });
    if (walkMs > 0) items.push({ key: ItemKeys.INGREDIENT_WALK, kind: 'WALK', name: '', percent: (walkMs / pool) * 100 });
    return items.filter((i) => i.percent >= 0.05).sort((a, b) => b.percent - a.percent);
  },

  build(stats, productName, phrases, hiddenPackages = new Set(), hiddenItems = new Set(), topAppFallback = '') {
    const top = stats.apps.find((a) => !hiddenPackages.has(a.pkg) && a.ms > 0);
    const topApp = top ? top.label : topAppFallback;
    const fill = (p) => p && { ...p, text: p.text.split('{topApp}').join(topApp) };
    return {
      productName,
      periodStart: stats.weekStart,
      periodEnd: stats.weekStart + 6,
      ingredients: this.ingredients(stats, hiddenPackages),
      nutrition: {
        days: stats.days, screenMs: stats.screenMs, unlocks: stats.unlocks,
        steps: stats.steps, avgSleepMs: this.avgSleepMs(stats),
      },
      cautions: phrases.cautions.map(fill),
      storage: fill(phrases.storage),
      expiry: fill(phrases.expiry),
      hidden: hiddenItems,
    };
  },
};

// ---------- 문구 엔진 (domain/phrase/*.kt) ----------

const PhraseMetrics = {
  from(stats) {
    const days = stats.days;
    const hasScreen = days > 0 && stats.screenMs > 0;
    const avgSleep = LabelBuilder.avgSleepMs(stats);
    return {
      lateNightRatio: hasScreen ? stats.lateNightMs / stats.screenMs : NaN,
      dailyScreenHours: hasScreen ? stats.screenMs / days / HOUR : NaN,
      dailySteps: days > 0 && stats.steps > 0 ? stats.steps / days : NaN,
      avgSleepHours: avgSleep != null ? avgSleep / HOUR : NaN,
      dailyUnlocks: days > 0 && stats.unlocks > 0 ? stats.unlocks / days : NaN,
      topAppShare: hasScreen && stats.apps.length ? stats.apps[0].ms / stats.screenMs : NaN,
    };
  },
};

function conditionHolds(c, metrics) {
  const m = metrics[c.metric];
  if (m == null || Number.isNaN(m)) return false;
  switch (c.op) {
    case '>': return m > c.value;
    case '>=': return m >= c.value;
    case '<': return m < c.value;
    case '<=': return m <= c.value;
    default: return false;
  }
}

/** 결정적 난수(미리보기용 시드). */
function seededRandom(seed) {
  let a = (seed >>> 0) || 1;
  return () => {
    a = (a + 0x6D2B79F5) >>> 0;
    let t = a;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

function shuffled(list, rnd) {
  const a = list.slice();
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(rnd() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

const PhraseSelector = {
  select(bank, category, metrics, recent, rnd, exclude = new Set()) {
    const def = bank.categories[category];
    if (!def) return [];
    const matched = def.groups.filter((g) => g.when.every((c) => conditionHolds(c, metrics)));
    const specific = matched.filter((g) => g.when.length > 0);
    const priorities = [...new Set(specific.map((g) => g.priority))].sort((a, b) => b - a);
    const ordered = priorities.flatMap((p) => shuffled(specific.filter((g) => g.priority === p), rnd));
    const defaults = matched.filter((g) => g.when.length === 0);

    const result = [];
    for (const group of [...ordered, ...defaults]) {
      if (result.length >= def.pick) break;
      const p = this.pickFrom(category, group, recent, new Set([...exclude, ...result.map((r) => r.id)]), rnd);
      if (p) result.push(p);
    }
    let guard = 0;
    while (result.length < def.pick && defaults.length && guard++ < def.pick * 4) {
      const group = defaults[Math.floor(rnd() * defaults.length)];
      const p = this.pickFrom(category, group, recent, new Set([...exclude, ...result.map((r) => r.id)]), rnd);
      if (p) result.push(p);
    }
    return result;
  },

  pickFrom(category, group, recent, exclude, rnd) {
    const candidates = group.phrases.map((_, i) => `${category}.${group.id}.${i}`).filter((id) => !exclude.has(id));
    if (!candidates.length) return null;
    const fresh = candidates.filter((id) => !recent.has(id));
    const pool = fresh.length ? fresh : candidates;
    const id = pool[Math.floor(rnd() * pool.length)];
    return { id, text: group.phrases[Number(id.split('.').pop())] };
  },

  /** 이 원자료에서 조건이 맞는 그룹(실험실 표시용). */
  matchedGroups(bank, category, metrics) {
    return bank.categories[category].groups.filter((g) => g.when.length && g.when.every((c) => conditionHolds(c, metrics)));
  },
};

function phraseText(bank, id) {
  const [cat, group, idx] = id.split('.');
  const g = bank.categories[cat]?.groups.find((x) => x.id === group);
  return g?.phrases[Number(idx)] ?? null;
}
