/* User Management System — Signal hi-fi + Material 3 mechanics. Prefix .um */
(function () {
  if (document.getElementById('um-styles')) return;
  const s = document.createElement('style');
  s.id = 'um-styles';
  s.textContent = `
  .um{--bg:#f7f7f5;--surf:#ffffff;--ink:#14151a;--ink2:#5d5f66;--ink3:#9a9ca3;
      --line:#e6e7ea;--line2:#eff0f2;--accent:#2d5bff;--accent-soft:#e9eeff;
      --green:#17935a;--green-bg:#e6f4ec;--amber:#c07a12;--amber-bg:#f7eedd;
      --danger:#d24b4b;--danger-bg:#fbecec;--skel:#e9eaed;--skel-hi:#f4f5f6;--btext:#2c2e34;
      --inv:#1c1d22;--inv-ink:#f4f5f6;--elev:0 10px 28px rgba(20,21,26,.16);--fab-elev:0 8px 20px rgba(45,91,255,.30);
      position:relative;width:100%;height:100%;background:var(--bg);color:var(--ink);
      font-family:'Space Grotesk',sans-serif;display:flex;flex-direction:column;overflow:hidden;
      -webkit-font-smoothing:antialiased;}
  .um.dark{--bg:#0e0f13;--surf:#181a20;--ink:#f1f2f5;--ink2:#9a9ca4;--ink3:#62656e;
      --line:#262932;--line2:#1d2027;--accent:#5b82ff;--accent-soft:rgba(91,130,255,.16);
      --green:#34c77b;--green-bg:rgba(52,199,123,.15);--amber:#e0a33a;--amber-bg:rgba(224,163,58,.15);
      --danger:#f0716e;--danger-bg:rgba(240,113,110,.14);--skel:#23262f;--skel-hi:#2c303a;--btext:#d3d5da;
      --inv:#e7e8ec;--inv-ink:#16171c;--elev:0 12px 30px rgba(0,0,0,.5);--fab-elev:0 8px 22px rgba(0,0,0,.55);}

  .um *{box-sizing:border-box;}
  .um .mono{font-family:'JetBrains Mono',monospace;}

  /* status bar */
  .um .stat{display:flex;justify-content:space-between;align-items:center;padding:13px 18px 7px;
      font-family:'JetBrains Mono',monospace;font-size:11px;font-weight:500;color:var(--ink2);flex:0 0 auto;}

  /* top app bar */
  .um .top{padding:8px 16px 12px;border-bottom:1px solid var(--line);flex:0 0 auto;background:var(--surf);}
  .um .kick{font-family:'JetBrains Mono',monospace;font-size:10px;font-weight:500;letter-spacing:.14em;text-transform:uppercase;color:var(--ink3);}
  .um .h{display:flex;align-items:center;gap:9px;margin-top:6px;}
  .um .h .ttl{font-size:23px;font-weight:700;letter-spacing:-.02em;}
  .um .h .ct{font-family:'JetBrains Mono',monospace;font-size:12px;color:var(--accent);background:var(--accent-soft);padding:2px 7px;border-radius:5px;font-weight:600;}
  .um .h .sp{flex:1;}
  .um .ibtn{width:36px;height:36px;border-radius:9px;border:1px solid var(--line);background:var(--surf);display:flex;align-items:center;justify-content:center;color:var(--ink2);flex:0 0 auto;}
  .um .ibtn.on{border-color:var(--accent);color:var(--accent);background:var(--accent-soft);}
  .um .ibtn svg{width:18px;height:18px;}

  /* list */
  .um .list{flex:1;overflow:hidden;padding:12px 12px 0;display:flex;flex-direction:column;gap:8px;}
  .um .row{display:flex;align-items:center;gap:12px;padding:11px 12px;background:var(--surf);border:1px solid var(--line);border-radius:12px;position:relative;}
  .um .av{width:42px;height:42px;border-radius:11px;flex:0 0 auto;display:flex;align-items:center;justify-content:center;font-family:'JetBrains Mono',monospace;font-size:13px;font-weight:600;background:var(--bg);color:var(--ink);border:1px solid var(--line);}
  .um .av.acc{background:var(--accent);color:#fff;border-color:var(--accent);}
  .um .meta{flex:1;min-width:0;}
  .um .nm{font-size:14px;font-weight:600;letter-spacing:-.01em;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}
  .um .em{font-family:'JetBrains Mono',monospace;font-size:11px;color:var(--ink2);margin-top:3px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}
  .um .right{display:flex;flex-direction:column;align-items:flex-end;gap:6px;flex:0 0 auto;}
  .um .ago{font-family:'JetBrains Mono',monospace;font-size:10px;font-weight:500;color:var(--ink3);white-space:nowrap;}

  /* chips */
  .um .chip{display:inline-flex;align-items:center;gap:5px;font-family:'JetBrains Mono',monospace;font-size:10px;font-weight:600;text-transform:uppercase;letter-spacing:.04em;padding:4px 8px;border-radius:5px;flex:0 0 auto;}
  .um .chip .d{width:6px;height:6px;border-radius:1.5px;background:currentColor;}
  .um .chip.act{color:var(--green);background:var(--green-bg);}
  .um .chip.off{color:var(--ink3);background:var(--line2);}
  .um .chip.neu{color:var(--ink2);background:var(--line2);}

  /* FAB (M3) */
  .um .fab{position:absolute;right:16px;bottom:18px;width:56px;height:56px;border-radius:18px;background:var(--accent);color:#fff;display:flex;align-items:center;justify-content:center;box-shadow:var(--fab-elev);z-index:6;}
  .um .fab svg{width:24px;height:24px;}

  /* offline / refresh banner */
  .um .banner{display:flex;align-items:center;gap:9px;margin:12px 12px 0;padding:9px 12px;border-radius:10px;font-size:11.5px;font-weight:500;flex:0 0 auto;}
  .um .banner.off{background:var(--line2);border:1px solid var(--line);color:var(--ink2);}
  .um .banner.busy{background:var(--accent-soft);color:var(--accent);}
  .um .banner.err{background:var(--danger-bg);color:var(--danger);}
  .um .banner svg{width:15px;height:15px;flex:0 0 auto;}
  .um .banner .when{margin-left:auto;font-family:'JetBrains Mono',monospace;font-size:10px;color:var(--ink3);font-weight:500;}
  .um .banner.busy .when{color:var(--accent);}

  /* shimmer */
  .um .sk{position:relative;overflow:hidden;background:var(--skel);border-radius:5px;}
  .um .sk::after{content:"";position:absolute;inset:0;transform:translateX(-100%);
      background:linear-gradient(90deg,transparent 20%,var(--skel-hi) 50%,transparent 80%);animation:umsh 1.4s ease-in-out infinite;}
  @keyframes umsh{to{transform:translateX(100%);}}
  .um .sk.circ{border-radius:11px;}

  /* centered states */
  .um .state{flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;gap:13px;padding:30px;}
  .um .state .ic{width:64px;height:64px;border-radius:18px;border:1.5px solid var(--line);background:var(--surf);display:flex;align-items:center;justify-content:center;color:var(--ink3);}
  .um .state .ic svg{width:28px;height:28px;}
  .um .state .ic.acc{color:var(--accent);border-color:var(--accent);background:var(--accent-soft);}
  .um .state .ic.err{color:var(--danger);border-color:var(--danger);}
  .um .state .st{font-size:17px;font-weight:700;letter-spacing:-.01em;}
  .um .state .sb{font-size:12.5px;color:var(--ink2);line-height:1.55;max-width:235px;}
  .um .state .code{font-family:'JetBrains Mono',monospace;font-size:10px;color:var(--ink3);letter-spacing:.05em;}

  /* buttons */
  .um .btn{height:42px;padding:0 20px;border-radius:11px;border:none;background:var(--accent);color:#fff;font-family:'Space Grotesk',sans-serif;font-size:13.5px;font-weight:600;display:inline-flex;align-items:center;justify-content:center;gap:8px;letter-spacing:-.01em;}
  .um .btn svg{width:17px;height:17px;}
  .um .btn.ghost{background:transparent;border:1px solid var(--line);color:var(--ink);}
  .um .btn.dgr{background:var(--danger);}
  .um .btn.txt{background:transparent;color:var(--accent);padding:0 14px;}
  .um .btn.txt.dgr{color:var(--danger);}
  .um .btn.block{width:100%;}
  .um .btn[disabled],.um .btn.dis{background:var(--line);color:var(--ink3);box-shadow:none;}
  .um .um.dark .btn.dis{background:var(--line2);}

  /* scrim + bottom sheet (M3) */
  .um .scrim{position:absolute;inset:0;background:rgba(10,11,15,.46);z-index:7;}
  .um .sheet{position:absolute;left:0;right:0;bottom:0;background:var(--surf);border-top-left-radius:24px;border-top-right-radius:24px;
      padding:10px 18px 20px;z-index:8;display:flex;flex-direction:column;gap:14px;box-shadow:var(--elev);border-top:1px solid var(--line);}
  .um .grab{width:34px;height:4px;border-radius:2px;background:var(--line);margin:2px auto 4px;flex:0 0 auto;}
  .um .sheet-h{display:flex;align-items:center;gap:8px;}
  .um .sheet-h .sh-ttl{font-size:18px;font-weight:700;letter-spacing:-.02em;}
  .um .sheet-h .sh-x{margin-left:auto;width:30px;height:30px;border-radius:8px;display:flex;align-items:center;justify-content:center;color:var(--ink3);}
  .um .sheet-h .sh-x svg{width:16px;height:16px;}

  /* M3 text fields (outlined) */
  .um .field{display:flex;flex-direction:column;gap:6px;}
  .um .field .lbl{font-size:11px;font-weight:600;color:var(--ink2);letter-spacing:.01em;}
  .um .field .lbl .req{color:var(--danger);}
  .um .inp{height:46px;border:1.5px solid var(--line);border-radius:11px;background:var(--surf);display:flex;align-items:center;padding:0 13px;font-size:13.5px;color:var(--ink);gap:8px;}
  .um .inp .val{flex:1;min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}
  .um .inp .val.mono{font-family:'JetBrains Mono',monospace;font-size:12.5px;}
  .um .inp .ph{color:var(--ink3);}
  .um .inp.focus{border-color:var(--accent);box-shadow:0 0 0 3px var(--accent-soft);}
  .um .inp.valid{border-color:var(--green);}
  .um .inp.err{border-color:var(--danger);}
  .um .inp .ic{flex:0 0 auto;display:flex;}
  .um .inp .ic svg{width:17px;height:17px;}
  .um .inp .ic.ok{color:var(--green);}
  .um .inp .ic.bad{color:var(--danger);}
  .um .caret{width:1.5px;height:19px;background:var(--accent);animation:umcaret 1.1s step-end infinite;}
  @keyframes umcaret{50%{opacity:0;}}
  .um .hint{font-family:'JetBrains Mono',monospace;font-size:10px;color:var(--ink3);letter-spacing:.02em;}
  .um .hint.err{color:var(--danger);}

  /* segmented (M3) */
  .um .seg-row{display:flex;gap:0;border:1.5px solid var(--line);border-radius:11px;overflow:hidden;}
  .um .seg{flex:1;text-align:center;font-size:12px;font-weight:600;padding:11px 0;color:var(--ink2);background:var(--surf);border-right:1.5px solid var(--line);display:flex;align-items:center;justify-content:center;gap:6px;}
  .um .seg:last-child{border-right:none;}
  .um .seg.on{background:var(--accent-soft);color:var(--accent);}
  .um .seg.on svg{width:14px;height:14px;}

  /* snackbar (M3) */
  .um .snack{position:absolute;left:14px;right:14px;bottom:16px;background:var(--inv);color:var(--inv-ink);border-radius:13px;
      padding:13px 8px 13px 16px;display:flex;align-items:center;gap:10px;z-index:9;box-shadow:var(--elev);overflow:hidden;}
  .um .snack .sk-txt{font-size:13px;font-weight:500;flex:1;}
  .um .snack .sk-act{font-size:12.5px;font-weight:700;letter-spacing:.04em;text-transform:uppercase;color:var(--accent);padding:6px 12px;border-radius:8px;flex:0 0 auto;}
  .um .snack.err .sk-act{color:var(--inv-ink);text-decoration:underline;}
  .um .snack .sk-bar{position:absolute;left:0;bottom:0;height:3px;background:var(--accent);}
  .um .snack.err{background:var(--danger);color:#fff;}

  /* confirm dialog (M3) */
  .um .dialog{position:absolute;left:26px;right:26px;top:50%;transform:translateY(-50%);background:var(--surf);border:1px solid var(--line);
      border-radius:20px;padding:22px 20px 16px;z-index:9;display:flex;flex-direction:column;gap:10px;box-shadow:var(--elev);}
  .um .dialog .dg-ic{width:46px;height:46px;border-radius:13px;background:var(--danger-bg);color:var(--danger);display:flex;align-items:center;justify-content:center;margin-bottom:2px;}
  .um .dialog .dg-ic svg{width:22px;height:22px;}
  .um .dialog .dg-ttl{font-size:18px;font-weight:700;letter-spacing:-.02em;}
  .um .dialog .dg-bd{font-size:13px;color:var(--ink2);line-height:1.5;}
  .um .dialog .dg-actions{display:flex;justify-content:flex-end;gap:6px;margin-top:8px;}

  /* press / removing / added / restored */
  .um .row.pressed{transform:scale(.98);box-shadow:0 0 0 2px var(--accent);}
  .um .row.pressed .rp{position:absolute;inset:0;border-radius:12px;background:radial-gradient(circle at 28% 50%,var(--accent-soft),transparent 60%);pointer-events:none;}
  .um .row.removing{opacity:.4;border-style:dashed;border-color:var(--danger);}
  .um .row.flash{border-color:var(--accent);box-shadow:inset 3px 0 0 var(--accent),0 0 0 1px var(--accent);}

  /* iPad split */
  .um.pad{flex-direction:row;}
  .um .pane{height:100%;display:flex;flex-direction:column;overflow:hidden;position:relative;}
  .um .pane.lp{flex:0 0 340px;border-right:1px solid var(--line);background:var(--bg);}
  .um .pane.lp.narrow{flex:0 0 280px;}
  .um .pane.dp{flex:1;background:var(--bg);}
  .um .row.sel{border-color:var(--accent);background:var(--accent-soft);box-shadow:inset 3px 0 0 var(--accent);}

  /* action panel */
  .um .ap-hero{padding:24px 26px;border-bottom:1px solid var(--line);display:flex;gap:18px;align-items:center;flex:0 0 auto;}
  .um .ap-hero .big{width:66px;height:66px;border-radius:16px;background:var(--accent);color:#fff;flex:0 0 auto;display:flex;align-items:center;justify-content:center;font-family:'JetBrains Mono',monospace;font-size:22px;font-weight:600;}
  .um .ap-hero .nm2{font-size:24px;font-weight:700;letter-spacing:-.02em;}
  .um .ap-hero .em2{font-family:'JetBrains Mono',monospace;font-size:12.5px;color:var(--ink2);margin-top:5px;}
  .um .ap-hero .hchips{display:flex;gap:7px;margin-top:12px;}
  .um .ap-body{flex:1;padding:22px 26px;display:flex;flex-direction:column;gap:18px;overflow:hidden;}
  .um .kv-grid{display:grid;grid-template-columns:1fr 1fr;gap:18px 24px;max-width:460px;}
  .um .kv .k{font-family:'JetBrains Mono',monospace;font-size:10px;font-weight:600;letter-spacing:.1em;text-transform:uppercase;color:var(--ink3);}
  .um .kv .v{font-size:14px;font-weight:600;color:var(--ink);margin-top:4px;}
  .um .sec{display:flex;align-items:center;gap:8px;font-family:'JetBrains Mono',monospace;font-size:10.5px;font-weight:600;letter-spacing:.1em;text-transform:uppercase;color:var(--ink3);}
  .um .sec .ln{flex:1;height:1px;background:var(--line);}
  .um .panel-empty{flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:14px;text-align:center;padding:40px;}
  .um .panel-empty .ic{width:72px;height:72px;border-radius:20px;border:1.5px dashed var(--line);display:flex;align-items:center;justify-content:center;color:var(--ink3);}
  .um .panel-empty .ic svg{width:30px;height:30px;}
  .um .panel-empty .pe-t{font-size:18px;font-weight:700;letter-spacing:-.01em;}
  .um .panel-empty .pe-b{font-size:13px;color:var(--ink2);line-height:1.5;max-width:300px;}
  `;
  document.head.appendChild(s);
})();

/* ---------- icons ---------- */
const I = {
  plus: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round"><path d="M12 5v14M5 12h14"/></svg>,
  refresh: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M21 12a9 9 0 1 1-2.64-6.36"/><path d="M21 3v6h-6"/></svg>,
  x: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><path d="M18 6 6 18M6 6l12 12"/></svg>,
  check: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round"><path d="M20 6 9 17l-5-5"/></svg>,
  warn: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 8v5"/><circle cx="12" cy="16.5" r=".5"/><path d="M10.3 3.8 2.4 18a1.9 1.9 0 0 0 1.7 2.9h15.8a1.9 1.9 0 0 0 1.7-2.9L13.7 3.8a1.9 1.9 0 0 0-3.4 0Z"/></svg>,
  alert: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 8v4"/><circle cx="12" cy="16" r=".5"/></svg>,
  users: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M16 19v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 19v-2a4 4 0 0 0-3-3.87M16 3.13A4 4 0 0 1 16 11"/></svg>,
  wifi: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12.5a10 10 0 0 1 14 0M8.5 16a5 5 0 0 1 7 0"/><circle cx="12" cy="19.5" r=".6"/><path d="M2 2l20 20"/></svg>,
  cloud: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M17.5 19a4.5 4.5 0 0 0 .5-9 6 6 0 0 0-11.6-1.5A4 4 0 0 0 6.5 19Z"/><path d="M2 2l20 20"/></svg>,
  trash: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M3 6h18M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/></svg>,
  edit: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>,
  spin: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round"><path d="M12 3a9 9 0 1 0 9 9" opacity="1"/></svg>,
};

const USERS = [
  ['AS', 'Aarav Sharma', 'aarav.sharma@example.com', 'act', 'just now'],
  ['PN', 'Priya Nair', 'priya.nair@example.com', 'act', '5 min ago'],
  ['MB', 'Marcus Bauch', 'marcus.bauch@example.com', 'off', '2 h ago'],
  ['EV', 'Elena Voss', 'elena.voss@example.com', 'act', '3 h ago'],
  ['TL', 'Tobias Lang', 'tobias.lang@example.com', 'off', '1 d ago'],
  ['YT', 'Yuki Tanaka', 'yuki.tanaka@example.com', 'act', '2 d ago'],
];

/* ---------- primitives ---------- */
const wrap = (dark, extra) => 'um' + (dark ? ' dark' : '') + (extra ? ' ' + extra : '');
const Stat = ({ net }) => (
  <div className="stat"><span>9:41</span><span>{net === 'off' ? '✕ offline · 84%' : '5G · 84%'}</span></div>
);
const Chip = ({ k }) => (
  <span className={'chip ' + (k === 'act' ? 'act' : 'off')}><span className="d"></span>{k === 'act' ? 'Active' : 'Inactive'}</span>
);
const Ib = ({ icon, on }) => (<span className={'ibtn' + (on ? ' on' : '')}>{icon}</span>);

const Row = ({ u, cls }) => (
  <div className={'row' + (cls ? ' ' + cls : '')}>
    {cls && cls.indexOf('pressed') > -1 ? <span className="rp"></span> : null}
    <div className={'av' + (cls && cls.indexOf('flash') > -1 ? ' acc' : '')}>{u[0]}</div>
    <div className="meta"><div className="nm">{u[1]}</div><div className="em">{u[2]}</div></div>
    <div className="right"><Chip k={u[3]} /><span className="ago">{u[4]}</span></div>
  </div>
);

const Fab = () => (<div className="fab">{I.plus}</div>);

const TopBar = ({ count, refreshing }) => (
  <div className="top">
    <div className="kick">// directory</div>
    <div className="h">
      <span className="ttl">Users</span>
      {count != null && <span className="ct">{count}</span>}
      <span className="sp"></span>
      <Ib icon={I.refresh} on={refreshing} />
    </div>
  </div>
);

/* ============================================================ FEED STATES */
function Feed({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar count="24" />
      <div className="list">{USERS.map((u) => <Row key={u[1]} u={u} />)}</div>
      <Fab />
    </div>
  );
}

function FeedLoading({ dark }) {
  const Sk = () => (
    <div className="row">
      <div className="sk circ" style={{ width: 42, height: 42, flex: '0 0 auto' }}></div>
      <div className="meta" style={{ display: 'flex', flexDirection: 'column', gap: 7 }}>
        <div className="sk" style={{ height: 11, width: '52%' }}></div>
        <div className="sk" style={{ height: 9, width: '84%' }}></div>
      </div>
      <div className="right">
        <div className="sk" style={{ width: 52, height: 18, borderRadius: 5 }}></div>
        <div className="sk" style={{ width: 36, height: 9, borderRadius: 4 }}></div>
      </div>
    </div>
  );
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar />
      <div className="list">{[0, 1, 2, 3, 4, 5].map((i) => <Sk key={i} />)}</div>
      <Fab />
    </div>
  );
}

function FeedEmpty({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar count="0" />
      <div className="state">
        <div className="ic acc">{I.users}</div>
        <div className="st">No users yet</div>
        <div className="sb">Add the first person to get started — they’ll appear right at the top of the feed.</div>
        <button className="btn">{I.plus} Add user</button>
      </div>
      <Fab />
    </div>
  );
}

function FeedError({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar />
      <div className="state">
        <div className="ic err">{I.warn}</div>
        <div className="st">Couldn’t load users</div>
        <div className="sb">Something went wrong on our side. Nothing was lost — give it another try.</div>
        <div className="code">ERR_500 · /v2/users</div>
        <button className="btn">{I.refresh} Retry</button>
      </div>
    </div>
  );
}

function FeedNoNet({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat net="off" />
      <TopBar />
      <div className="state">
        <div className="ic">{I.wifi}</div>
        <div className="st">You’re offline</div>
        <div className="sb">No connection, and nothing’s been cached yet. Reconnect to load your users.</div>
        <div className="code">NO_NETWORK · cache empty</div>
        <button className="btn ghost">{I.refresh} Try again</button>
      </div>
    </div>
  );
}

function FeedOffline({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat net="off" />
      <TopBar count="24" />
      <div className="banner off">{I.cloud}<span>Offline — showing cached users</span><span className="when">updated 12 min ago</span></div>
      <div className="list">{USERS.slice(0, 5).map((u) => <Row key={u[1]} u={[u[0], u[1], u[2], u[3], '12 min ago']} />)}</div>
      <Fab />
    </div>
  );
}

function FeedRefreshing({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar count="24" refreshing />
      <div className="banner busy">{I.refresh}<span>Refreshing…</span></div>
      <div className="list">{USERS.slice(0, 5).map((u) => <Row key={u[1]} u={u} />)}</div>
      <Fab />
    </div>
  );
}

/* ============================================================ ADD USER */
const muted = { filter: 'saturate(.55)', opacity: .45 };
function FeedBehind({ n = 3 }) {
  return <div className="list" style={muted}>{USERS.slice(0, n).map((u) => <Row key={u[1]} u={u} />)}</div>;
}

function AddEmpty({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar count="24" />
      <FeedBehind />
      <div className="scrim"></div>
      <div className="sheet">
        <div className="grab"></div>
        <div className="sheet-h"><span className="sh-ttl">Add user</span><span className="sh-x">{I.x}</span></div>
        <div className="field"><span className="lbl">Name <span className="req">*</span></span><div className="inp"><span className="val ph">Full name</span></div></div>
        <div className="field"><span className="lbl">Email <span className="req">*</span></span><div className="inp"><span className="val ph mono">name@example.com</span></div></div>
        <div className="field"><span className="lbl">Gender</span><div className="seg-row"><span className="seg on">Female</span><span className="seg">Male</span><span className="seg">Other</span></div></div>
        <div className="field"><span className="lbl">Status</span><div className="seg-row"><span className="seg on">Active</span><span className="seg">Inactive</span></div></div>
        <button className="btn block dis">Add user</button>
      </div>
    </div>
  );
}

function AddValidating({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar count="24" />
      <FeedBehind n={2} />
      <div className="scrim"></div>
      <div className="sheet">
        <div className="grab"></div>
        <div className="sheet-h"><span className="sh-ttl">Add user</span><span className="sh-x">{I.x}</span></div>
        <div className="field"><span className="lbl">Name <span className="req">*</span></span><div className="inp valid"><span className="val">Maya Reed</span><span className="ic ok">{I.check}</span></div></div>
        <div className="field"><span className="lbl">Email <span className="req">*</span></span><div className="inp err focus"><span className="val mono">maya.reed@</span><span className="caret"></span><span className="ic bad">{I.alert}</span></div><span className="hint err">Enter a valid email address</span></div>
        <div className="field"><span className="lbl">Gender</span><div className="seg-row"><span className="seg on">Female</span><span className="seg">Male</span><span className="seg">Other</span></div></div>
        <div className="field"><span className="lbl">Status</span><div className="seg-row"><span className="seg on">Active</span><span className="seg">Inactive</span></div></div>
        <button className="btn block dis">Add user</button>
      </div>
    </div>
  );
}

function AddSubmitting({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar count="24" />
      <FeedBehind n={2} />
      <div className="scrim"></div>
      <div className="sheet">
        <div className="grab"></div>
        <div className="sheet-h"><span className="sh-ttl">Add user</span></div>
        <div className="field"><span className="lbl">Name <span className="req">*</span></span><div className="inp valid"><span className="val">Maya Reed</span><span className="ic ok">{I.check}</span></div></div>
        <div className="field"><span className="lbl">Email <span className="req">*</span></span><div className="inp valid"><span className="val mono">maya.reed@example.com</span><span className="ic ok">{I.check}</span></div></div>
        <div className="field"><span className="lbl">Gender</span><div className="seg-row"><span className="seg on">Female</span><span className="seg">Male</span><span className="seg">Other</span></div></div>
        <button className="btn block" style={{ opacity: .9 }}><span className="um-spin" style={{ display: 'inline-flex', animation: 'umspin .8s linear infinite' }}>{I.spin}</span> Adding…</button>
      </div>
      <style>{`@keyframes umspin{to{transform:rotate(360deg);}}`}</style>
    </div>
  );
}

function AddSuccess({ dark }) {
  const newU = ['MR', 'Maya Reed', 'maya.reed@example.com', 'act', 'just now'];
  const rest = [USERS[0], USERS[1], USERS[2], USERS[3]];
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar count="25" />
      <div className="list">
        <Row u={newU} cls="flash" />
        {rest.map((u) => <Row key={u[1]} u={u} />)}
      </div>
      <Fab />
      <div className="snack"><span className="sk-txt">Maya Reed added</span></div>
    </div>
  );
}

function AddError({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar count="24" />
      <FeedBehind n={2} />
      <div className="scrim"></div>
      <div className="sheet">
        <div className="grab"></div>
        <div className="sheet-h"><span className="sh-ttl">Add user</span><span className="sh-x">{I.x}</span></div>
        <div className="banner err" style={{ margin: 0 }}>{I.alert}<span>Couldn’t add user — please try again</span></div>
        <div className="field"><span className="lbl">Name <span className="req">*</span></span><div className="inp valid"><span className="val">Maya Reed</span><span className="ic ok">{I.check}</span></div></div>
        <div className="field"><span className="lbl">Email <span className="req">*</span></span><div className="inp valid"><span className="val mono">maya.reed@example.com</span><span className="ic ok">{I.check}</span></div></div>
        <button className="btn block">{I.refresh} Try again</button>
      </div>
    </div>
  );
}

/* ============================================================ DELETE + UNDO */
function DelPress({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar count="24" />
      <div className="list">
        <Row u={USERS[0]} />
        <Row u={USERS[1]} cls="pressed" />
        <Row u={USERS[2]} />
        <Row u={USERS[3]} />
        <Row u={USERS[4]} />
      </div>
      <Fab />
    </div>
  );
}

function DelConfirm({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar count="24" />
      <FeedBehind n={4} />
      <div className="scrim"></div>
      <div className="dialog">
        <div className="dg-ic">{I.trash}</div>
        <div className="dg-ttl">Delete Priya Nair?</div>
        <div className="dg-bd">This removes them from your feed. You’ll be able to undo right after.</div>
        <div className="dg-actions">
          <button className="btn txt">Cancel</button>
          <button className="btn dgr" style={{ height: 38 }}>Delete</button>
        </div>
      </div>
    </div>
  );
}

function DelRemoving({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar count="23" />
      <div className="list">
        <Row u={USERS[0]} />
        <Row u={USERS[1]} cls="removing" />
        <Row u={USERS[2]} />
        <Row u={USERS[3]} />
        <Row u={USERS[4]} />
      </div>
      <div className="snack">
        <span className="sk-txt">Priya Nair deleted</span>
        <span className="sk-act">Undo</span>
        <span className="sk-bar" style={{ width: '58%' }}></span>
      </div>
    </div>
  );
}

function DelRestored({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar count="24" />
      <div className="list">
        <Row u={USERS[0]} />
        <Row u={USERS[1]} cls="flash" />
        <Row u={USERS[2]} />
        <Row u={USERS[3]} />
        <Row u={USERS[4]} />
      </div>
      <Fab />
    </div>
  );
}

function DelFailed({ dark }) {
  return (
    <div className={wrap(dark)}>
      <Stat />
      <TopBar count="24" />
      <div className="list">{USERS.slice(0, 5).map((u) => <Row key={u[1]} u={u} />)}</div>
      <Fab />
      <div className="snack err">
        <span className="sk-txt">Couldn’t delete — Priya Nair restored</span>
        <span className="sk-act">Retry</span>
      </div>
    </div>
  );
}

/* ============================================================ iPad */
const PadList = ({ selName, narrow, add }) => (
  <div className={'pane lp' + (narrow ? ' narrow' : '')}>
    <div className="stat"><span>9:41</span><span>Wi-Fi · 100%</span></div>
    <div className="top">
      <div className="kick">// directory</div>
      <div className="h">
        <span className="ttl">Users</span><span className="ct">24</span><span className="sp"></span>
        <Ib icon={I.plus} on={add} /><Ib icon={I.refresh} />
      </div>
    </div>
    <div className="list" style={{ gap: 7 }}>
      {USERS.map((u) => (
        <Row key={u[1]} u={[u[0], u[1], u[2], u[3], u[4].replace(' ago', '').replace('just now', 'now')]} cls={u[1] === selName ? 'sel' : ''} />
      ))}
    </div>
  </div>
);

function PadSelected({ dark }) {
  return (
    <div className={wrap(dark, 'pad')}>
      <PadList selName="Priya Nair" />
      <div className="pane dp">
        <div className="stat" style={{ justifyContent: 'flex-end' }}><span>usr_8841</span></div>
        <div className="ap-hero">
          <div className="big">PN</div>
          <div>
            <div className="nm2">Priya Nair</div>
            <div className="em2">priya.nair@example.com</div>
            <div className="hchips"><span className="chip act"><span className="d"></span>Active</span><span className="chip neu">Female</span></div>
          </div>
        </div>
        <div className="ap-body">
          <div className="kv-grid">
            <div className="kv"><div className="k">Last active</div><div className="v">5 minutes ago</div></div>
            <div className="kv"><div className="k">User ID</div><div className="v mono" style={{ fontFamily: "'JetBrains Mono',monospace", fontSize: 13 }}>#2 · usr_8841</div></div>
            <div className="kv"><div className="k">Status</div><div className="v">Active</div></div>
            <div className="kv"><div className="k">Gender</div><div className="v">Female</div></div>
          </div>
          <div className="sec">Actions<span className="ln"></span></div>
          <div style={{ display: 'flex', gap: 10 }}>
            <button className="btn dgr">{I.trash} Delete user</button>
            <button className="btn ghost">{I.edit} Edit details</button>
          </div>
        </div>
      </div>
    </div>
  );
}

function PadAdd({ dark }) {
  return (
    <div className={wrap(dark, 'pad')}>
      <PadList add />
      <div className="pane dp">
        <div className="stat" style={{ justifyContent: 'flex-end' }}><span>new user</span></div>
        <div className="ap-hero" style={{ paddingBottom: 18 }}>
          <div>
            <div className="nm2">Add user</div>
            <div className="em2" style={{ fontFamily: "'Space Grotesk',sans-serif", fontSize: 13 }}>New people appear at the top of the feed</div>
          </div>
          <span className="sh-x" style={{ marginLeft: 'auto', width: 34, height: 34, border: '1px solid var(--line)', borderRadius: 9, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--ink3)' }}>{I.x}</span>
        </div>
        <div className="ap-body" style={{ maxWidth: 520, gap: 18 }}>
          <div className="field"><span className="lbl">Name <span className="req">*</span></span><div className="inp valid"><span className="val">Maya Reed</span><span className="ic ok">{I.check}</span></div></div>
          <div className="field"><span className="lbl">Email <span className="req">*</span></span><div className="inp focus"><span className="val mono">maya.reed@example.com</span><span className="caret"></span></div></div>
          <div style={{ display: 'flex', gap: 18 }}>
            <div className="field" style={{ flex: 1 }}><span className="lbl">Gender</span><div className="seg-row"><span className="seg on">Female</span><span className="seg">Male</span><span className="seg">Other</span></div></div>
            <div className="field" style={{ flex: 1 }}><span className="lbl">Status</span><div className="seg-row"><span className="seg on">Active</span><span className="seg">Inactive</span></div></div>
          </div>
          <div style={{ display: 'flex', gap: 10, marginTop: 4 }}>
            <button className="btn ghost">Cancel</button>
            <button className="btn block">{I.plus} Add user</button>
          </div>
        </div>
      </div>
    </div>
  );
}

function PadEmpty({ dark }) {
  return (
    <div className={wrap(dark, 'pad')}>
      <PadList />
      <div className="pane dp">
        <div className="stat" style={{ justifyContent: 'flex-end' }}><span>—</span></div>
        <div className="panel-empty">
          <div className="ic">{I.users}</div>
          <div className="pe-t">Select a user</div>
          <div className="pe-b">Pick someone from the list to see their details and manage them — or use ＋ to add a new user.</div>
          <button className="btn">{I.plus} Add user</button>
        </div>
      </div>
    </div>
  );
}

function PadPortrait({ dark }) {
  return (
    <div className={wrap(dark, 'pad')}>
      <PadList selName="Priya Nair" narrow />
      <div className="pane dp">
        <div className="stat" style={{ justifyContent: 'flex-end' }}><span>usr_8841</span></div>
        <div className="ap-hero" style={{ padding: '20px 22px' }}>
          <div className="big" style={{ width: 56, height: 56, fontSize: 18 }}>PN</div>
          <div>
            <div className="nm2" style={{ fontSize: 21 }}>Priya Nair</div>
            <div className="em2">priya.nair@example.com</div>
            <div className="hchips"><span className="chip act"><span className="d"></span>Active</span><span className="chip neu">Female</span></div>
          </div>
        </div>
        <div className="ap-body" style={{ padding: '20px 22px' }}>
          <div className="kv-grid">
            <div className="kv"><div className="k">Last active</div><div className="v">5 minutes ago</div></div>
            <div className="kv"><div className="k">User ID</div><div className="v" style={{ fontFamily: "'JetBrains Mono',monospace", fontSize: 13 }}>#2</div></div>
          </div>
          <div className="sec">Actions<span className="ln"></span></div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            <button className="btn dgr block">{I.trash} Delete user</button>
            <button className="btn ghost block">{I.edit} Edit details</button>
          </div>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, {
  Feed, FeedLoading, FeedEmpty, FeedError, FeedNoNet, FeedOffline, FeedRefreshing,
  AddEmpty, AddValidating, AddSubmitting, AddSuccess, AddError,
  DelPress, DelConfirm, DelRemoving, DelRestored, DelFailed,
  PadSelected, PadAdd, PadEmpty, PadPortrait,
});
