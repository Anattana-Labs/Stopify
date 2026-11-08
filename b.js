/******/
(() => {
    // webpackBootstrap
    /******/
    "use strict";

    ;// ../../node_modules/uuid/dist/esm-browser/native.js
    const randomUUID = typeof crypto !== 'undefined' && crypto.randomUUID && crypto.randomUUID.bind(crypto);
    /* harmony default export */
    const esm_browser_native = ({
        randomUUID
    });
    ;// ../../node_modules/uuid/dist/esm-browser/rng.js
    // Unique ID creation requires a high quality random # generator. In the browser we therefore
    // require the crypto API and do not support built-in fallback to lower quality random number
    // generators (like Math.random()).
    let getRandomValues;
    const rnds8 = new Uint8Array(16);
    function rng() {
        // lazy load so that environments that need to polyfill have a chance to do so
        if (!getRandomValues) {
            // getRandomValues needs to be invoked in a context where "this" is a Crypto implementation.
            getRandomValues = typeof crypto !== 'undefined' && crypto.getRandomValues && crypto.getRandomValues.bind(crypto);

            if (!getRandomValues) {
                throw new Error('crypto.getRandomValues() not supported. See https://github.com/uuidjs/uuid#getrandomvalues-not-supported');
            }
        }

        return getRandomValues(rnds8);
    }
    ;// ../../node_modules/uuid/dist/esm-browser/stringify.js
    /* unused harmony import specifier */
    var validate;

    /**
 * Convert array of 16 byte values to UUID string format of the form:
 * XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
 */

    const byteToHex = [];

    for (let i = 0; i < 256; ++i) {
        byteToHex.push((i + 0x100).toString(16).slice(1));
    }

    function unsafeStringify(arr, offset = 0) {
        // Note: Be careful editing this code!  It's been tuned for performance
        // and works in ways you may not expect. See https://github.com/uuidjs/uuid/pull/434
        return byteToHex[arr[offset + 0]] + byteToHex[arr[offset + 1]] + byteToHex[arr[offset + 2]] + byteToHex[arr[offset + 3]] + '-' + byteToHex[arr[offset + 4]] + byteToHex[arr[offset + 5]] + '-' + byteToHex[arr[offset + 6]] + byteToHex[arr[offset + 7]] + '-' + byteToHex[arr[offset + 8]] + byteToHex[arr[offset + 9]] + '-' + byteToHex[arr[offset + 10]] + byteToHex[arr[offset + 11]] + byteToHex[arr[offset + 12]] + byteToHex[arr[offset + 13]] + byteToHex[arr[offset + 14]] + byteToHex[arr[offset + 15]];
    }

    function stringify(arr, offset = 0) {
        const uuid = unsafeStringify(arr, offset);
        // Consistency check for valid UUID.  If this throws, it's likely due to one
        // of the following:
        // - One or more input array values don't map to a hex octet (leading to
        // "undefined" in the uuid)
        // - Invalid input values for the RFC `version` or `variant` fields

        if (!validate(uuid)) {
            throw TypeError('Stringified UUID is invalid');
        }

        return uuid;
    }

    /* harmony default export */
    const esm_browser_stringify = ((/* unused pure expression or super */
        null && (stringify)));
    ;// ../../node_modules/uuid/dist/esm-browser/v4.js

    function v4(options, buf, offset) {
        if (esm_browser_native.randomUUID && !buf && !options) {
            return esm_browser_native.randomUUID();
        }

        options = options || {};
        const rnds = options.random || (options.rng || rng)();
        // Per 4.4, set bits for version and `clock_seq_hi_and_reserved`

        rnds[6] = rnds[6] & 0x0f | 0x40;
        rnds[8] = rnds[8] & 0x3f | 0x80;
        // Copy bytes to buffer, if provided

        if (buf) {
            offset = offset || 0;

            for (let i = 0; i < 16; ++i) {
                buf[offset + i] = rnds[i];
            }

            return buf;
        }

        return unsafeStringify(rnds);
    }

    /* harmony default export */
    const esm_browser_v4 = (v4);
    ;// ../../node_modules/@eyeo/snippets/webext/main.mjs
    /*!
 * This file is part of eyeo's Anti-Circumvention Snippets module (@eyeo/snippets),
 * Copyright (C) 2006-present eyeo GmbH
 * 
 * @eyeo/snippets is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * 
 * @eyeo/snippets is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with @eyeo/snippets.  If not, see <http://www.gnu.org/licenses/>.
 */
    let currentEnvironment = {
        initial: true
    };
    const callback = (environment, ...filters) => {
        const e = Proxy
            , { apply: t, bind: n, call: r } = Function
            , o = r.bind(t)
            , i = r.bind(n)
            , s = r.bind(r)
            , a = {
                get: (e, t) => i(r, e[t])
            }
            , c = t => new e(t, a)
            , l = (t, n) => new e(t, {
                apply: (e, t, r) => o(n, t, r)
            })
            , u = {
                get: (e, t) => i(e[t], e)
            }
            , f = t => new e(t, u)
            , { assign: p, defineProperties: d, freeze: h, getOwnPropertyDescriptor: y, getOwnPropertyDescriptors: g, getPrototypeOf: m } = f(Object)
            , { hasOwnProperty: w } = c({})
            , { species: v } = Symbol
            , b = {
                get(e, t) {
                    const n = e[t];
                    class r extends n {
                    }
                    const o = g(n.prototype);
                    delete o.constructor,
                        h(d(r.prototype, o));
                    const i = g(n);
                    return delete i.length,
                        delete i.prototype,
                        i[v] = {
                            value: r
                        },
                        h(d(r, i))
                }
            }
            , E = t => new e(t, b);
        "undefined" != typeof currentEnvironment && currentEnvironment.initial && "undefined" != typeof environment && (currentEnvironment = environment);
        const $ = () => "undefined" != typeof currentEnvironment ? currentEnvironment : "undefined" != typeof environment ? environment : {};
        "undefined" == typeof globalThis && (window.globalThis = window);
        const { apply: S, ownKeys: R } = f(Reflect)
            , x = $()
            , P = "world" in x
            , k = P && "ISOLATED" === x.world
            , O = P && "MAIN" === x.world
            , T = "object" == typeof chrome && !!chrome.runtime
            , j = "object" == typeof browser && !!browser.runtime
            , A = !O && (k || T || j)
            , L = e => A ? e : N(e, D(e))
            , { create: N, defineProperties: I, defineProperty: M, freeze: C, getOwnPropertyDescriptor: F, getOwnPropertyDescriptors: D } = f(Object)
            , q = f(globalThis)
            , W = A ? globalThis : E(globalThis)
            , { Map: H, RegExp: J, Set: _, WeakMap: B, WeakSet: V } = W
            , U = (e, t, n = null) => {
                const r = R(t);
                for (const o of R(e)) {
                    if (r.includes(o))
                        continue;
                    const i = F(e, o);
                    if (n && "value" in i) {
                        const { value: e } = i;
                        "function" == typeof e && (i.value = n(e))
                    }
                    M(t, o, i)
                }
            }
            , z = e => {
                const t = W[e];
                class n extends t {
                }
                const { toString: r, valueOf: o } = t.prototype;
                I(n.prototype, {
                    toString: {
                        value: r
                    },
                    valueOf: {
                        value: o
                    }
                });
                const i = e.toLowerCase()
                    , s = e => function () {
                        const t = S(e, this, arguments);
                        return typeof t === i ? new n(t) : t
                    }
                    ;
                return U(t, n, s),
                    U(t.prototype, n.prototype, s),
                    n
            }
            , X = C({
                frozen: new B,
                hidden: new V,
                iframePropertiesToAbort: {
                    read: new _,
                    write: new _
                },
                abortedIframes: new B
            })
            , G = new J("^[A-Z]")
            , K = A && (T && chrome || j && browser) || void 0;
        var Q = new Proxy(new H([["chrome", K], ["browser", K], ["isExtensionContext", A], ["variables", X], ["console", L(console)], ["document", globalThis.document], ["JSON", L(JSON)], ["Map", H], ["Math", L(Math)], ["Number", A ? Number : z("Number")], ["RegExp", J], ["Set", _], ["String", A ? String : z("String")], ["WeakMap", B], ["WeakSet", V], ["MouseEvent", MouseEvent]]), {
            get(e, t) {
                if (e.has(t))
                    return e.get(t);
                let n = globalThis[t];
                return "function" == typeof n && (n = (G.test(t) ? W : q)[t]),
                    e.set(t, n),
                    n
            },
            has: (e, t) => e.has(t)
        });
        const Y = {
            WeakSet: WeakSet,
            WeakMap: WeakMap,
            WeakValue: class {
                has() {
                    return !1
                }
                set() { }
            }
        }
            , { apply: Z } = Reflect;
        const { Map: ee, WeakMap: te, WeakSet: ne, setTimeout: re } = Q;
        let oe = !0
            , ie = e => {
                e.clear(),
                    oe = !oe
            }
            ;
        var se = function (e) {
            const { WeakSet: t, WeakMap: n, WeakValue: r } = this || Y
                , o = new t
                , i = new n
                , s = new r;
            return function (t) {
                if (o.has(t))
                    return t;
                if (i.has(t))
                    return i.get(t);
                if (s.has(t))
                    return s.get(t);
                const n = Z(e, this, arguments);
                return o.add(n),
                    n !== t && ("object" == typeof t && t ? i : s).set(t, n),
                    n
            }
        }
            .bind({
                WeakMap: te,
                WeakSet: ne,
                WeakValue: class extends ee {
                    set(e, t) {
                        return oe && (oe = !oe,
                            re(ie, 0, this)),
                            super.set(e, t)
                    }
                }
            });
        const { concat: ae, includes: ce, join: le, reduce: ue, unshift: fe } = c([])
            , { Map: pe, WeakMap: de } = E(globalThis)
            , he = new pe
            , ye = e => {
                const t = (e => {
                    const t = [];
                    let n = e;
                    for (; n;) {
                        if (he.has(n))
                            fe(t, he.get(n));
                        else {
                            const e = g(n);
                            he.set(n, e),
                                fe(t, e)
                        }
                        n = m(n)
                    }
                    return fe(t, {}),
                        o(p, null, t)
                }
                )("function" == typeof e ? e.prototype : e)
                    , n = {
                        get(e, n) {
                            if (n in t) {
                                const { value: r, get: o } = t[n];
                                if (o)
                                    return s(o, e);
                                if ("function" == typeof r)
                                    return i(r, e)
                            }
                            return e[n]
                        },
                        set(e, n, r) {
                            if (n in t) {
                                const { set: o } = t[n];
                                if (o)
                                    return s(o, e, r),
                                        !0
                            }
                            return e[n] = r,
                                !0
                        }
                    };
                return e => new Proxy(e, n)
            }
            , { isExtensionContext: ge, Array: me, Number: we, String: ve, Object: be } = Q
            , { isArray: Ee } = me
            , { getOwnPropertyDescriptor: $e, setPrototypeOf: Se } = be
            , { toString: Re } = be.prototype
            , { slice: xe } = ve.prototype
            , { get: Pe } = $e(Node.prototype, "nodeType")
            , ke = ge ? {} : {
                Attr: ye(Attr),
                CanvasRenderingContext2D: ye(CanvasRenderingContext2D),
                CSSStyleDeclaration: ye(CSSStyleDeclaration),
                Document: ye(Document),
                Element: ye(Element),
                HTMLCanvasElement: ye(HTMLCanvasElement),
                HTMLElement: ye(HTMLElement),
                HTMLImageElement: ye(HTMLImageElement),
                HTMLScriptElement: ye(HTMLScriptElement),
                MutationRecord: ye(MutationRecord),
                Node: ye(Node),
                ShadowRoot: ye(ShadowRoot),
                get CSS2Properties() {
                    return ke.CSSStyleDeclaration
                }
            }
            , Oe = (e, t) => {
                if ("Element" !== t && t in ke)
                    return ke[t](e);
                if (Ee(e))
                    return Se(e, me.prototype);
                const n = (e => s(xe, s(Re, e), 8, -1))(e);
                if (n in ke)
                    return ke[n](e);
                if (n in Q)
                    return Se(e, Q[n].prototype);
                if ("nodeType" in e)
                    switch (s(Pe, e)) {
                        case 1:
                            if (!(t in ke))
                                throw new Error("unknown hint " + t);
                            return ke[t](e);
                        case 2:
                            return ke.Attr(e);
                        case 3:
                            return ke.Node(e);
                        case 9:
                            return ke.Document(e)
                    }
                throw new Error("unknown brand " + n)
            }
            ;
        var Te = ge ? e => e === window || e === globalThis ? Q : e : se(((e, t = "Element") => {
            if (e === window || e === globalThis)
                return Q;
            switch (typeof e) {
                case "object":
                    return e && Oe(e, t);
                case "string":
                    return new ve(e);
                case "number":
                    return new we(e);
                default:
                    throw new Error("unsupported value")
            }
        }
        ));
        const je = {
            get(e, t) {
                const n = e;
                for (; !w(e, t);)
                    e = m(e);
                const { get: r, set: i } = y(e, t);
                return function () {
                    return arguments.length ? o(i, n, arguments) : s(r, n)
                }
            }
        }
            , Ae = t => new e(t, je);
        let { Array: Le, document: Ne, JSON: Ie, Math: Me, Object: Ce, RegExp: Fe, Set: De } = Te(window);
        function qe(e, t) {
            const n = "/" === e[0] ? Te(e).lastIndexOf("/") : 0;
            if (n > 0) {
                const r = Te(e).slice(1, n)
                    , o = Te(e).slice(n + 1);
                try {
                    return new Fe(r, o)
                } catch (e) {
                    t && t("warn", `malformed regex /${r}/${o} (${e.message}), treating pattern as literal text`)
                }
            }
            return new Fe(Te(e).replace(/[-/\\^$*+?.()|[\]{}]/g, "\\$&"))
        }
        function We(e, t) {
            return e.lastIndex = 0,
                Te(e).test(t)
        }
        function He(e, t) {
            const n = qe(e, t)
                , r = Te(n.flags).includes("g") ? n.flags : n.flags + "g";
            return new Fe(n.source, r)
        }
        function Je(e, t) {
            const n = []
                , r = e ? Te(e).trim().split(/\s+/) : [];
            t && t("info", `Needle tokens: '${r.join("', '")}'`);
            for (const e of r)
                if (e)
                    if (Te(e).startsWith("url:")) {
                        const r = Te(e).slice(4).toString();
                        if (!r) {
                            t && t("warn", "empty 'url:' token in needle, ignoring it");
                            continue
                        }
                        n.push({
                            target: "url",
                            re: qe(r, t)
                        })
                    } else
                        n.push({
                            target: "body",
                            re: qe(e, t)
                        });
            return ({ body: e, url: t }) => {
                for (const { target: r, re: o } of n) {
                    const n = "url" === r ? t : e;
                    if (null == n || !We(o, n))
                        return !1
                }
                return !0
            }
        }
        function _e(e) {
            try {
                return Ie.parse(e)
            } catch (t) {
                return e
            }
        }
        function Be(e, t, n) {
            let r = e[t];
            return Le.isArray(r) ? (Le.isArray(n) ? e[t] = Te(r).concat(n) : Te(r).push(n),
                !0) : "object" != typeof r || null === r || "object" != typeof n || null === n || Le.isArray(n) ? "string" != typeof r || "string" != typeof n && "number" != typeof n ? (e[t] = n,
                    !1) : (e[t] = r + Te(n).toString(),
                        !0) : (Ce.assign(r, n),
                            !0)
        }
        function Ve(e) {
            const t = {}
                , n = new De;
            Te(e).forEach((({ parent: e, key: r }) => {
                Le.isArray(e) ? (e[r] = t,
                    n.add(e)) : delete e[r]
            }
            )),
                n.forEach((e => {
                    for (let n = e.length - 1; n >= 0; n--)
                        e[n] === t && Te(e).splice(n, 1)
                }
                ))
        }
        function Ue(e) {
            const t = $();
            if ("function" == typeof t.sendSnippetHitEvent)
                try {
                    t.sendSnippetHitEvent(e, Ne.location.hostname)
                } catch (e) { }
        }
        function ze() {
            return Te(Me.floor(2116316160 * Me.random() + 60466176)).toString(36)
        }
        function Xe(e) {
            return Te(Le.from(e)).map((e => `'${e}'`)).join(" ")
        }
        let { Math: Ge, setInterval: Ke, performance: Qe } = Te(window);
        const Ye = {
            mark() { },
            end() { },
            toString: () => "{mark(){},end(){}}"
        };
        let Ze = !0;
        function et(e, t = 10) {
            if (Ze)
                return Ye;
            function n() {
                let e = Te([]);
                for (let { name: t, duration: n } of Qe.getEntriesByType("measure"))
                    e.push({
                        name: t,
                        duration: n
                    });
                e.length && Qe.clearMeasures()
            }
            return et[e] || (et[e] = Ke(n, Ge.round(6e4 / Ge.min(60, t)))),
            {
                mark() {
                    Qe.mark(e)
                },
                end(t = !1) {
                    Qe.measure(e, e);
                    const r = Qe.getEntriesByName(e, "measure")
                        , o = r.length > 0 ? r[r.length - 1] : null;
                    console.log("PROFILER:", o),
                        Qe.clearMarks(e),
                        t && (clearInterval(et[e]),
                            delete et[e],
                            n())
                }
            }
        }
        let tt = !1
            , nt = null;
        function rt() {
            return tt
        }
        const { console: ot } = Te(window)
            , it = () => { }
            ;
        function st(...e) {
            let { mark: t, end: n } = et("log");
            if (rt()) {
                const t = ["%c DEBUG", "font-weight: bold;"]
                    , n = e.indexOf("error")
                    , r = e.indexOf("warn")
                    , o = e.indexOf("success")
                    , i = e.indexOf("info");
                -1 !== n ? (t[0] += " - ERROR",
                    t[1] += "color: red; border:2px solid red",
                    Te(e).splice(n, 1)) : -1 !== r ? (t[0] += " - WARNING",
                        t[1] += "color: orange; border:2px solid orange ",
                        Te(e).splice(r, 1)) : -1 !== o ? (t[0] += " - SUCCESS",
                            t[1] += "color: green; border:2px solid green",
                            Te(e).splice(o, 1)) : -1 !== i && (t[1] += "color: black;",
                                Te(e).splice(i, 1)),
                    Te(e).unshift(...t);
                const s = nt;
                if (s) {
                    if (!Te(e).some((e => We(s, e))))
                        return
                }
            }
            t(),
                ot.log(...e),
                n()
        }
        function at(e) {
            return i(rt() ? st : it, null, e)
        }
        const { Function: ct, Object: lt, WeakMap: ut } = Te(window);
        let ft = !1;
        const pt = new ut;
        function dt(e, t) {
            ft || function () {
                const { toString: e } = ct.prototype
                    , t = l(e, (function () {
                        const t = pt.get(this);
                        return o(e, void 0 !== t ? t : this, arguments)
                    }
                    ));
                lt.defineProperty(window.Function.prototype, "toString", {
                    value: t
                }),
                    pt.set(t, e),
                    ft = !0
            }(),
                pt.set(e, t)
        }
        let { parseFloat: ht, variables: yt, clearTimeout: gt, fetch: mt, setTimeout: wt, Array: vt, Error: bt, Map: Et, Object: $t, ReferenceError: St, Set: Rt, WeakMap: xt } = Te(window)
            , { onerror: Pt } = Ae(window)
            , kt = Node.prototype
            , Ot = Element.prototype
            , Tt = null;
        function jt(e, t, n, r = !0) {
            let o = Te(t)
                , i = o.indexOf(".");
            if (-1 == i) {
                let o = $t.getOwnPropertyDescriptor(e, t);
                if (o && !o.configurable)
                    return;
                let i = $t.assign({}, n, {
                    configurable: r
                });
                if (!o && !i.get && i.set) {
                    let n = e[t];
                    i.get = () => n
                }
                return void $t.defineProperty(e, t, i)
            }
            let s = o.slice(0, i).toString();
            t = o.slice(i + 1).toString();
            let a = e[s];
            !a || "object" != typeof a && "function" != typeof a || jt(a, t, n, r);
            let c = $t.getOwnPropertyDescriptor(e, s);
            if (c && !c.configurable)
                return;
            Tt || (Tt = new xt),
                Tt.has(e) || Tt.set(e, new Et);
            let l = Tt.get(e);
            if (l.has(s))
                return void l.get(s).set(t, n);
            let u = new Et([[t, n]]);
            l.set(s, u),
                $t.defineProperty(e, s, {
                    get: () => a,
                    set(e) {
                        if (a = e,
                            a && ("object" == typeof a || "function" == typeof a))
                            for (let [e, t] of u)
                                jt(a, e, t, r)
                    },
                    configurable: r
                })
        }
        function At(e) {
            let t = Pt();
            Pt(((...n) => {
                let r = n.length && n[0];
                return !("string" != typeof r || !Te(r).includes(e)) || ("function" == typeof t ? o(t, this, n) : void 0)
            }
            ))
        }
        function Lt(e, t, n, r = "", o = !0) {
            let i = at(e);
            if (!n)
                return void i("error", "no property to abort on read");
            let s = ze()
                , a = !1;
            i("info", `aborting on ${n} access`),
                jt(t, n, {
                    get: function () {
                        throw i("success", `${n} access aborted`, `\nFILTER: ${e} ${r}`),
                        a || (a = !0,
                            Ue(`${e} ${r}`)),
                        new St(s)
                    },
                    set() { }
                }, o),
                At(s)
        }
        function Nt(e, t, n, r = "", o = !0) {
            let i = at(e);
            if (!n)
                return void i("error", "no property to abort on write");
            let s = ze()
                , a = !1;
            i("info", `aborting when setting ${n}`),
                jt(t, n, {
                    set: function () {
                        throw i("success", `setting ${n} aborted`, `\nFILTER: ${e} ${r}`),
                        a || (a = !0,
                            Ue(`${e} ${r}`)),
                        new St(s)
                    }
                }, o),
                At(s)
        }
        function It(e, t = !1, n = !1) {
            let r = yt.abortedIframes
                , i = yt.iframePropertiesToAbort;
            const a = Xe(e);
            for (let o of vt.from(window.frames))
                if (r.has(o))
                    for (let i of e)
                        t && r.get(o).read.add({
                            property: i,
                            formattedProperties: a
                        }),
                            n && r.get(o).write.add({
                                property: i,
                                formattedProperties: a
                            });
            for (let r of e)
                t && i.read.add({
                    property: r,
                    formattedProperties: a
                }),
                    n && i.write.add({
                        property: r,
                        formattedProperties: a
                    });
            function c() {
                for (let e of vt.from(window.frames)) {
                    r.has(e) || r.set(e, {
                        read: new Rt(i.read),
                        write: new Rt(i.write)
                    });
                    let t = r.get(e).read;
                    if (t.size > 0) {
                        let n = vt.from(t);
                        t.clear();
                        for (let { property: t, formattedProperties: r } of n)
                            Lt("abort-on-iframe-property-read", e, t, r)
                    }
                    let n = r.get(e).write;
                    if (n.size > 0) {
                        let t = vt.from(n);
                        n.clear();
                        for (let { property: n, formattedProperties: r } of t)
                            Nt("abort-on-iframe-property-write", e, n, r)
                    }
                }
            }
            c(),
                r.has(document) || (r.set(document, !0),
                    function (e) {
                        let t;
                        function n(e, t) {
                            for (let n of t) {
                                jt(e, n, r(e, n))
                            }
                        }
                        function r(t, n) {
                            let r = t[n]
                                , i = function (...t) {
                                    let n;
                                    return n = o(r, this, t),
                                        e && e(),
                                        n
                                };
                            return dt(i, r),
                            {
                                get: () => i
                            }
                        }
                        function i(t, n) {
                            let r = $t.getOwnPropertyDescriptor(t, n)
                                , { set: o } = r || {};
                            return {
                                set(t) {
                                    let n;
                                    return n = s(o, this, t),
                                        e && e(),
                                        n
                                }
                            }
                        }
                        n(kt, ["appendChild", "insertBefore", "replaceChild"]),
                            n(Ot, ["append", "prepend", "replaceWith", "after", "before", "insertAdjacentElement", "insertAdjacentHTML"]),
                            t = i(Ot, "innerHTML"),
                            jt(Ot, "innerHTML", t),
                            t = i(Ot, "outerHTML"),
                            jt(Ot, "outerHTML", t)
                    }(c))
        }
        let { Object: Mt } = window;
        function Ct(e, t) {
            if (!(e instanceof Mt))
                return;
            let n = e
                , r = Te(t).split(".");
            if (0 === r.length)
                return;
            for (let e = 0; e < r.length - 1; e++) {
                let t = r[e];
                if (!w(n, t))
                    return;
                if (n = n[t],
                    !(n instanceof Mt))
                    return
            }
            let o = r[r.length - 1];
            return w(n, o) ? [n, o] : void 0
        }
        const Ft = Te(/^\d+$/);
        function Dt(e) {
            switch (e) {
                case "false":
                    return !1;
                case "true":
                    return !0;
                case "falseStr":
                    return "false";
                case "trueStr":
                    return "true";
                case "null":
                    return null;
                case "noopFunc":
                    return () => { }
                        ;
                case "trueFunc":
                    return () => !0;
                case "falseFunc":
                    return () => !1;
                case "emptyArray":
                    return [];
                case "emptyObj":
                    return {};
                case "undefined":
                    return;
                case "":
                    return e;
                default:
                    return Ft.test(e) ? ht(e) : e
            }
        }
        function qt(e, t) {
            if (!e || !e.length)
                return !0;
            const n = ze()
                , r = new bt(n)
                , o = new URL(self.location.href);
            o.hash = "";
            const i = /(.*?@)?(\S+)(:\d+):\d+\)?$/
                , s = [];
            for (let e of r.stack.split(/[\n\r]+/)) {
                if (Te(e).includes(n))
                    continue;
                e = Te(e).trim();
                const t = Te(i).exec(e);
                if (null === t)
                    continue;
                let r = t[2];
                Te(r).startsWith("(") && (r = Te(r).slice(1)),
                    r === o.href ? r = "inlineScript" : Te(r).startsWith("<anonymous>") && (r = "injectedScript");
                let a = t[1] ? Te(t[1]).slice(0, -1) : Te(e).slice(0, Te(t).index).trim();
                Te(a).startsWith("at") && (a = Te(a).slice(2).trim());
                let c = t[3];
                Te(s).push(" " + `${a} ${r}${c}:1`.trim())
            }
            s[0] = "stackDepth:" + (s.length - 1);
            const a = Te(s).join("\n");
            for (let n of e) {
                if (We(qe(n, t), a))
                    return t("info", `Found needle in stack trace: ${n}`),
                        !0
            }
            return t("info", `Stack trace does not match any needle. Stack trace: ${a}`),
                !1
        }
        new Et;
        let { HTMLScriptElement: Wt, Object: Ht, ReferenceError: Jt } = Te(window)
            , _t = Ht.getPrototypeOf(Wt);
        const { Error: Bt, Object: Vt, Array: Ut, Map: zt } = Te(window);
        let Xt = null;
        const Gt = new Set;
        function Kt(e) {
            Gt.has(e) || (Gt.add(e),
                Ue(e))
        }
        function Qt(e, t, n) {
            let r = e;
            for (const e of n) {
                if (!r || !w(r, e))
                    return !1;
                r = r[e]
            }
            if ("string" == typeof r || "number" == typeof r) {
                return We(t, r.toString())
            }
            return !1
        }
        const { Array: Yt, Blob: Zt, Error: en, Object: tn, Reflect: nn } = Te(window)
            , rn = []
            , on = new Set;
        let { Error: sn, URL: an } = Te(window)
            , { cookie: cn } = Ae(document);
        const { Map: ln, Object: un, Reflect: fn, WeakMap: pn } = Te(window)
            , dn = window.EventTarget.prototype.addEventListener
            , hn = window.EventTarget.prototype.removeEventListener
            , yn = new pn;
        let gn = [];
        const mn = new Set;
        function wn(e) {
            mn.has(e) || (mn.add(e),
                Ue(e))
        }
        let { console: vn, document: bn, getComputedStyle: En, isExtensionContext: $n, variables: Sn, Array: Rn, MutationObserver: xn, Object: Pn, DOMMatrix: kn, XPathEvaluator: On, XPathExpression: Tn, XPathResult: jn } = Te(window);
        const { querySelectorAll: An } = bn
            , Ln = An && i(An, bn);
        function Nn(e, t = !1) {
            return Cn(e, Ln.bind(bn), bn, t)
        }
        function In(e, t, n, r) {
            const o = t.getAttribute("xlink:href") || t.getAttribute("href");
            if (o) {
                const s = Ln(o)[0];
                if (!s && rt())
                    return vn.log("No elements found matching", o),
                        !1;
                if (!(i = e) || 0 === i.length || i.every((e => "" === e.trim()))) {
                    const e = r.length > 0 ? r : [];
                    return n.push({
                        element: s,
                        rootParents: [...e, t]
                    }),
                        !1
                }
                const a = s.querySelectorAll.bind(s);
                return {
                    nextBoundElement: s,
                    nestedSelectorsString: e.join("^^"),
                    next$$: a
                }
            }
            var i
        }
        function Mn(e, t) {
            const n = function (e, t = !1) {
                try {
                    const n = navigator.userAgent.includes("Firefox") ? e.openOrClosedShadowRoot : browser.dom.openOrClosedShadowRoot(e);
                    return null === n && rt() && !t && vn.log("Shadow root not found or not added in element yet", e),
                        n
                } catch (n) {
                    return rt() && !t && vn.log("Error while accessing shadow root", e, n),
                        null
                }
            }(t);
            if (n) {
                const { querySelectorAll: r } = n
                    , o = r && i(r, n).bind(n);
                return {
                    nextBoundElement: t,
                    nestedSelectorsString: ":host " + e.join("^^"),
                    next$$: o
                }
            }
            return !1
        }
        function Cn(e, t, n, r, o = []) {
            if (e.includes("^^")) {
                const [i, s, ...a] = e.split("^^");
                let c, l;
                switch (s) {
                    case "svg":
                        l = In;
                        break;
                    case "sh":
                        l = Mn;
                        break;
                    default:
                        return rt() && vn.log(s, " is not supported. Supported commands are: \n^^sh^^\n^^svg^^"),
                            []
                }
                c = "" === i.trim() ? [n] : t(i);
                const u = [];
                for (const e of c) {
                    const t = l(a, e, u, o);
                    if (!t)
                        continue;
                    const { next$$: n, nestedSelectorsString: i, nextBoundElement: s } = t
                        , c = Cn(i, n, s, r, [...o, e]);
                    c && u.push(...c)
                }
                return u
            }
            const i = t(e);
            return r ? [...i].map((e => ({
                element: e,
                rootParents: o.length > 0 ? o : []
            }))) : i
        }
        const { assign: Fn, setPrototypeOf: Dn } = Pn;
        class qn extends Tn {
            evaluate(...e) {
                return Dn(o(super.evaluate, this, e), jn.prototype)
            }
        }
        class Wn extends On {
            createExpression(...e) {
                return Dn(o(super.createExpression, this, e), qn.prototype)
            }
        }
        function Hn(e) {
            if (Sn.hidden.has(e))
                return !1;
            !function (e) {
                $n && "function" == typeof checkElement && checkElement(e)
            }(e),
                Sn.hidden.add(e);
            let { style: t } = Te(e)
                , n = Te(t, "CSSStyleDeclaration")
                , r = Te([]);
            const o = $();
            let { debugCSSProperties: i } = o;
            for (let [e, t] of i || [["display", "none"]])
                n.setProperty(e, t, "important"),
                    r.push([e, n.getPropertyValue(e)]);
            return new xn((() => {
                for (let [e, t] of r) {
                    let r = n.getPropertyValue(e)
                        , o = n.getPropertyPriority(e);
                    r == t && "important" == o || n.setProperty(e, t, "important")
                }
            }
            )).observe(e, {
                attributes: !0,
                attributeFilter: ["style"]
            }),
                !0
        }
        function Jn(e) {
            let t = e;
            if (t.startsWith("xpath(") && t.endsWith(")")) {
                let t = function (e) {
                    let t = e;
                    if (t.startsWith("xpath(") && t.endsWith(")")) {
                        let e = t.slice(6, -1)
                            , n = (new Wn).createExpression(e, null)
                            , r = jn.ORDERED_NODE_SNAPSHOT_TYPE;
                        return e => {
                            if (!e)
                                return;
                            let t = n.evaluate(bn, r, null)
                                , { snapshotLength: o } = t;
                            for (let n = 0; n < o; n++)
                                e(t.snapshotItem(n))
                        }
                    }
                    return t => Nn(e).forEach(t)
                }(e);
                return () => {
                    let e = Te([]);
                    return t((t => e.push(t))),
                        e
                }
            }
            return () => Rn.from(Nn(e))
        }
        let { ELEMENT_NODE: _n, TEXT_NODE: Bn, prototype: Vn } = Node
            , { prototype: Un } = Element
            , { prototype: zn } = HTMLElement
            , { console: Xn, variables: Gn, DOMParser: Kn, Error: Qn, MutationObserver: Yn, Object: Zn, ReferenceError: er } = Te(window)
            , { getOwnPropertyDescriptor: tr } = Zn;
        const { CanvasRenderingContext2D: nr, document: rr, Map: or, MutationObserver: ir, Object: sr, requestAnimationFrame: ar, Set: cr, WeakMap: lr, WeakSet: ur } = Te(window);
        let fr, pr = new lr, dr = new ur, hr = new cr, yr = new ur;
        const gr = new cr;
        let mr = !1
            , wr = !1
            , vr = !1
            , br = new cr;
        function Er(e, t) {
            dr.add(e),
                pr.delete(e);
            const n = Te(e).closest(t.selector);
            if (n && !yr.has(n)) {
                Hn(n),
                    yr.add(n),
                    at("hide-if-canvas-contains")("success", "Matched: ", n, `\nFILTER: hide-if-canvas-contains ${t.formattedArguments}`);
                const e = "hide-if-canvas-contains " + t.formattedArguments;
                gr.has(e) || (gr.add(e),
                    Ue(e))
            } else
                !function (e, t) {
                    hr.add({
                        canvasElement: e,
                        rule: t
                    })
                }(e, t)
        }
        function $r(e) {
            mr && e && !dr.has(e) && (br.add(e),
                vr || (vr = !0,
                    ar(Sr)))
        }
        function Sr() {
            vr = !1;
            const e = br;
            br = new cr;
            const t = at("hide-if-canvas-contains");
            for (const n of e) {
                if (dr.has(n))
                    continue;
                let e = null
                    , r = !1;
                for (const [o, i] of fr)
                    if ("data" === i.mode && Te(n).closest(i.selector) && !r) {
                        if (null === e)
                            try {
                                e = Te(n).toDataURL().toString()
                            } catch (e) {
                                t("info", "Could not read canvas data URL:", e.message),
                                    r = !0;
                                continue
                            }
                        We(o, e) && Er(n, i)
                    }
            }
        }
        Te(window);
        const { Map: Rr, MutationObserver: xr, Object: Pr, Set: kr, WeakSet: Or } = Te(window);
        let Tr = Element.prototype
            , { attachShadow: jr } = Tr
            , Ar = new Or
            , Lr = new Rr;
        const Nr = new kr;
        let Ir = null;
        const { Error: Mr, Object: Cr, Array: Fr, parseFloat: Dr, isNaN: qr } = Te(window);
        class Wr {
            constructor(e) {
                if ("string" != typeof e)
                    throw new Mr("JSONPath: query must be a string");
                if (!e.length)
                    throw new Mr("JSONPath: query must be a non-empty string");
                this._steps = this._tokenize(e)
            }
            _tokenize(e) {
                e = Te(e);
                const t = new Fr;
                let n = 0;
                for ("$" === e[0].toString() && (n = 1); n < e.length;) {
                    let r = !1;
                    if (e.startsWith("..", n) ? (r = !0,
                        n += 2) : "." === e[n].toString() && n++,
                        "[" === e[n].toString()) {
                        const o = e.indexOf("]", n);
                        if (-1 === o)
                            throw new Mr(`JSONPath: unclosed bracket in query "${e}"`);
                        const i = e.slice(n + 1, o);
                        if (!i.length)
                            throw new Mr(`JSONPath: empty bracket notation in query "${e}"`);
                        i.startsWith("?(") ? t.push({
                            type: "filter",
                            key: "?",
                            filter: this._parseFilter(i),
                            recursive: r
                        }) : t.push({
                            type: "direct",
                            key: i.replace(/['"]/g, "").toString(),
                            recursive: r
                        }),
                            n = o + 1
                    } else {
                        const o = e.slice(n).search(/[.[]/)
                            , i = -1 === o ? e.slice(n).toString() : e.slice(n, n + o).toString();
                        if (!i && !r)
                            throw new Mr(`JSONPath: trailing dot with no property name in query "${e}"`);
                        (i || r) && t.push({
                            type: "direct",
                            key: i || "*",
                            recursive: r
                        }),
                            n += i.length
                    }
                }
                return t
            }
            _parseFilter(e) {
                const t = (e = Te(e)).match(/(?:[@.]?)([\w]+(?:\.[\w]+)*)\s*([!=^$*]=|[<>]=?)\s*(?:['"](.+?)['"]|([\w.+-]+))\)/);
                if (!t)
                    throw new Mr(`JSONPath: invalid filter expression "${e}"`);
                return {
                    property: t[1],
                    operator: t[2],
                    target: null != t[3] ? t[3] : t[4]
                }
            }
            evaluate(e) {
                if (!e || "object" != typeof e)
                    throw new Mr("JSONPath: evaluate() requires an object or array");
                let t = Te([{
                    parent: {
                        root: e
                    },
                    key: "root"
                }]);
                for (const e of this._steps) {
                    const n = [];
                    for (const { parent: r, key: o } of t) {
                        const t = r[o];
                        t && "object" == typeof t && (e.recursive ? this._deepSearch(t, e, n) : this._match(t, e, n))
                    }
                    t = n
                }
                return t
            }
            _match(e, t, n) {
                const r = "*" === t.key || "?" === t.key ? Cr.keys(e) : [t.key];
                for (const o of r)
                    if (w(e, o)) {
                        if ("?" === t.key && !this._test(e[o], t.filter))
                            continue;
                        n.push({
                            parent: e,
                            key: o
                        })
                    }
            }
            _deepSearch(e, t, n, r = 1e4) {
                if (this._match(e, t, n),
                    !(r <= 0))
                    for (const o of Cr.keys(e))
                        e[o] && "object" == typeof e[o] && this._deepSearch(e[o], t, n, r - 1)
            }
            _test(e, t) {
                if (!t || !e)
                    return !1;
                let n = e;
                for (const e of Te(t.property).split(".")) {
                    if (null == n || "object" != typeof n)
                        return !1;
                    n = n[e]
                }
                const r = Te(n)
                    , o = Te(t.target)
                    , i = r.toString()
                    , s = o.toString()
                    , a = Dr(r)
                    , c = Dr(o)
                    , l = !qr(a) && !qr(c);
                switch (t.operator) {
                    case "==":
                        return l ? a === c : i === s;
                    case "!=":
                        return l ? a !== c : i !== s;
                    case "<":
                        return l ? a < c : i < s;
                    case "<=":
                        return l ? a <= c : i <= s;
                    case ">":
                        return l ? a > c : i > s;
                    case ">=":
                        return l ? a >= c : i >= s;
                    case "^=":
                        return r.startsWith(o);
                    case "$=":
                        return r.endsWith(o);
                    case "*=":
                        return r.includes(o);
                    default:
                        return !1
                }
            }
        }
        const { Array: Hr, Error: Jr, JSON: _r, Map: Br, Object: Vr, Response: Ur } = Te(window);
        let zr = null;
        const Xr = new Set;
        function Gr(e) {
            Xr.has(e) || (Xr.add(e),
                Ue(e))
        }
        let { Array: Kr, Error: Qr, JSON: Yr, Map: Zr, Object: eo, Response: to } = Te(window)
            , no = null;
        const ro = new Set;
        function oo(e) {
            ro.has(e) || (ro.add(e),
                Ue(e))
        }
        const { Error: io, Object: so, Map: ao } = Te(window);
        let co = null;
        const lo = new Set;
        function uo(e) {
            lo.has(e) || (lo.add(e),
                Ue(e))
        }
        function fo(e, t, n) {
            if (!n.length) {
                if ("string" == typeof e || "number" == typeof e) {
                    return We(t, e.toString())
                }
                return !1
            }
            let r = e;
            for (const e of n) {
                if (!r || !w(r, e))
                    return !1;
                r = r[e]
            }
            if ("string" == typeof r || "number" == typeof r) {
                return We(t, r.toString())
            }
            return !1
        }
        let { Error: po } = Te(window);
        const { Array: ho, addEventListener: yo, Error: go, Object: mo, Reflect: wo, Set: vo, WeakSet: bo } = Te(window)
            , Eo = new bo
            , $o = new ho
            , So = new vo
            , Ro = new vo;
        let { Error: xo, Map: Po, Object: ko, console: Oo } = Te(window)
            , { toString: To } = Function.prototype
            , jo = EventTarget.prototype
            , { addEventListener: Ao } = jo
            , Lo = null;
        const No = new Set;
        const Io = Proxy
            , { toStringTag: Mo } = Symbol
            , { defineProperty: Co, deleteProperty: Fo, get: Do, getOwnPropertyDescriptor: qo, has: Wo, set: Ho } = f(Reflect)
            , { Array: Jo, Error: _o, Map: Bo, Object: Vo, Set: Uo, document: zo, parseFloat: Xo, setTimeout: Go } = Te(window)
            , Ko = new Jo
            , Qo = new Uo;
        function Yo(e) {
            Qo.has(e) || (Qo.add(e),
                Ue(e))
        }
        const Zo = new Uo(["closed", "close", "opener", "frameElement", "parent", "top", "self", "window", "globalThis", "frames", "location", "document", "history", Mo]);
        let { Array: ei, Map: ti, Object: ni, parseInt: ri, Set: oi } = Te(window);
        const ii = new ti
            , si = new oi
            , ai = new oi;
        let { fetch: ci, Request: li } = Te(window)
            , ui = !1;
        const fi = []
            , pi = []
            , di = []
            , hi = () => {
                if (!ui) {
                    let e = l(ci, ((...e) => {
                        let [t, n] = e
                            , r = "string" == typeof t ? t : t instanceof URL ? t.href : t && "string" == typeof t.url ? t.url : "";
                        if (fi.length > 0 && "string" == typeof t) {
                            let n;
                            try {
                                n = new URL(t)
                            } catch (e) {
                                if (!(e instanceof TypeError))
                                    throw e;
                                n = new URL(t, Te(document).location)
                            }
                            fi.forEach((e => e(n))),
                                e[0] = n.href,
                                r = n.href
                        }
                        const i = e => o(ci, self, e).then((e => {
                            let t = e;
                            return pi.forEach((e => {
                                t = e(t, {
                                    url: r
                                })
                            }
                            )),
                                t
                        }
                        ))
                            , s = e => {
                                let t = e;
                                return di.forEach((e => {
                                    t = e(t, {
                                        url: r
                                    })
                                }
                                )),
                                    t
                            }
                            ;
                        if (di.length > 0)
                            if (n && "string" == typeof n.body) {
                                const t = s(n.body);
                                t !== n.body && (e[1] = Object.assign({}, n, {
                                    body: t
                                }))
                            } else if (t && "function" == typeof t.clone && (!n || null == n.body) && "GET" !== t.method && "HEAD" !== t.method)
                                return t.clone().text().then((n => {
                                    if (!n)
                                        return i(e);
                                    const r = s(n);
                                    if (r === n)
                                        return i(e);
                                    const o = new li(t, {
                                        body: r,
                                        referrer: t.referrer,
                                        referrerPolicy: t.referrerPolicy
                                    });
                                    return i([o, ...e.slice(1)])
                                }
                                )).catch((() => i(e)));
                        return i(e)
                    }
                    ));
                    dt(e, window.fetch),
                        window.fetch = e,
                        ui = !0
                }
            }
            ;
        let yi, { Error: gi, JSON: mi, Map: wi, Set: vi } = Te(window);
        const bi = new vi;
        let Ei, { JSON: $i, Map: Si, Object: Ri, Response: xi, Set: Pi } = Te(window);
        const ki = new Pi;
        const { Error: Oi, Object: Ti, atob: ji, btoa: Ai } = Te(window);
        let { XMLHttpRequest: Li, WeakMap: Ni, Object: Ii } = Te(window)
            , Mi = !1;
        const Ci = []
            , Fi = []
            , Di = new Ni
            , qi = () => {
                if (Mi)
                    return;
                const e = class extends Li {
                    open(e, t, ...n) {
                        return Di.set(this, {
                            url: t
                        }),
                            super.open(e, t, ...n)
                    }
                    send(e) {
                        let t = e;
                        if ("string" == typeof e && Ci.length > 0) {
                            const { url: e } = Di.get(this) || {};
                            for (const n of Ci)
                                t = n(t, {
                                    url: e
                                })
                        }
                        return super.send(t)
                    }
                    get response() {
                        const e = super.response;
                        if (0 === Fi.length)
                            return e;
                        const t = Di.get(this);
                        if (void 0 === t)
                            return e;
                        const n = "string" == typeof e ? e.length : void 0;
                        if (t.lastResponseLength !== n && (t.cachedResponse = void 0,
                            t.lastResponseLength = n),
                            void 0 !== t.cachedResponse)
                            return t.cachedResponse;
                        if ("string" != typeof e)
                            return t.cachedResponse = e;
                        let r = e;
                        for (const e of Fi)
                            r = e(r, {
                                url: t.url
                            });
                        return t.cachedResponse = r
                    }
                    get responseText() {
                        const e = this.response;
                        return "string" != typeof e ? super.responseText : e
                    }
                }
                    ;
                dt(e, window.XMLHttpRequest),
                    dt(e.prototype.open, window.XMLHttpRequest.prototype.open),
                    dt(e.prototype.send, window.XMLHttpRequest.prototype.send),
                    dt(Ii.getOwnPropertyDescriptor(e.prototype, "response").get, Ii.getOwnPropertyDescriptor(window.XMLHttpRequest.prototype, "response").get),
                    dt(Ii.getOwnPropertyDescriptor(e.prototype, "responseText").get, Ii.getOwnPropertyDescriptor(window.XMLHttpRequest.prototype, "responseText").get),
                    window.XMLHttpRequest = e,
                    Mi = !0
            }
            ;
        let Wi, { Error: Hi, JSON: Ji, Map: _i, Set: Bi } = Te(window);
        const Vi = new Bi;
        let Ui, { JSON: zi, Map: Xi, Set: Gi } = Te(window);
        function Ki(e) {
            let t = Dt(e);
            return t !== e ? t : _e(e)
        }
        const Qi = new Gi;
        let Yi, { delete: Zi, has: es } = c(URLSearchParams.prototype);
        const ts = new Set;
        const { Error: ns, Object: rs, parseInt: os, isNaN: is } = Te(window)
            , { toString: ss } = Function.prototype
            , as = window.setTimeout
            , cs = window.setInterval
            , ls = {
                TIMEOUT: "timeout",
                INTERVAL: "interval",
                BOTH: "both"
            };
        let us = null;
        const fs = new Set;
        const { Array: ps, Date: ds, Object: hs, Set: ys, WeakSet: gs, document: ms, parseInt: ws, window: vs } = Te(window);
        let bs = !1;
        const Es = "param_first"
            , $s = "param_second"
            , Ss = "pyv"
            , Rs = "client_screen"
            , xs = "ad_type"
            , Ps = "none"
            , ks = {
                [Es]: $s,
                [$s]: Ss,
                [Ss]: Rs,
                [Rs]: xs
            }
            , Os = "eAFgAQ"
            , Ts = "8AUB"
            , js = "CHANNEL"
            , As = ["playerErrorMessageRenderer", "UNPLAYABLE"]
            , Ls = ["AGE_CHECK_REQUIRED", "AGE_VERIFICATION_REQUIRED", "CONTENT_CHECK_REQUIRED", "LOGIN_REQUIRED"]
            , Ns = e => "string" == typeof e && -1 !== Ls.indexOf(e)
            , Is = ["gate", "preview", "shorts"];
        function Ms(e) {
            if ("string" != typeof e || 0 === e.length)
                return "homepage";
            let t = e;
            const n = t.indexOf("?");
            -1 !== n && (t = t.slice(0, n));
            const r = t.indexOf("#");
            -1 !== r && (t = t.slice(0, r));
            const o = t.indexOf("://");
            -1 !== o && (t = t.slice(o + 3));
            const i = t.indexOf("/");
            if (-1 === i)
                return "homepage";
            const s = /^\/([^/]+)/.exec(t.slice(i));
            return s ? s[1].toLowerCase() : "homepage"
        }
        const { Date: Cs, MutationObserver: Fs, Set: Ds, document: qs, parseInt: Ws, setTimeout: Hs, window: Js } = Te(window);
        let _s = !1;
        function Bs(e, t) {
            if (null == e)
                return;
            const n = e[t];
            if ("function" == typeof n)
                try {
                    return n.call(e)
                } catch (e) {
                    return
                }
        }
        function Vs(e, t) {
            const n = function (e) {
                if ("string" != typeof e || 0 === e.length)
                    return "";
                let t = e;
                const n = t.indexOf("?");
                -1 !== n && (t = t.slice(0, n));
                const r = t.indexOf("#");
                -1 !== r && (t = t.slice(0, r));
                const o = t.indexOf("://");
                -1 !== o && (t = t.slice(o + 3));
                const i = t.indexOf("/");
                if (-1 === i)
                    return "";
                const s = t.slice(i)
                    , a = /^\/([^/]+)/.exec(s);
                return a ? a[1].toLowerCase() : ""
            }(e);
            for (let e = 0; e < t.deny.length; e++)
                if (t.deny[e] === n)
                    return !1;
            if (0 === t.allow.length)
                return !0;
            for (let e = 0; e < t.allow.length; e++)
                if (t.allow[e] === n)
                    return !0;
            return !1
        }
        const Us = {
            "abort-current-inline-script": function (e, t = null) {
                const n = Xe(arguments)
                    , r = at("abort-current-inline-script")
                    , { mark: o, end: i } = et("abort-current-inline-script")
                    , a = t ? qe(t, r) : null
                    , c = ze()
                    , l = Te(document).currentScript;
                let u = !1
                    , f = window;
                const p = Te(e).split(".")
                    , d = Te(p).pop();
                for (let e of Te(p))
                    if (f = f[e],
                        !f || "object" != typeof f && "function" != typeof f)
                        return void r("warn", p, " is not found");
                const { get: h, set: y } = Ht.getOwnPropertyDescriptor(f, d) || {};
                let g = f[d];
                void 0 === g && r("warn", "The property", d, "doesn't exist yet. Check typos.");
                const m = () => {
                    const e = Te(document).currentScript;
                    if (e instanceof _t && "" == Te(e, "HTMLScriptElement").src && e != l && (!a || We(a, Te(e).textContent)))
                        throw r("success", p, " is aborted \n", e, "\nFILTER: abort-current-inline-script", n),
                        u || (u = !0,
                            Ue("abort-current-inline-script " + n)),
                        new Jt(c)
                }
                    , w = {
                        get() {
                            return m(),
                                h ? s(h, this) : g
                        },
                        set(e) {
                            m(),
                                y ? s(y, this, e) : g = e
                        }
                    };
                o(),
                    jt(f, d, w),
                    i(),
                    At(c)
            },
            "abort-on-iframe-property-read": function (...e) {
                const { mark: t, end: n } = et("abort-on-iframe-property-read");
                t(),
                    It(e, !0, !1),
                    n()
            },
            "abort-on-iframe-property-write": function (...e) {
                const { mark: t, end: n } = et("abort-on-iframe-property-write");
                t(),
                    It(e, !1, !0),
                    n()
            },
            "abort-on-property-read": function (e, t) {
                const n = !("false" === t)
                    , r = Xe(arguments)
                    , { mark: o, end: i } = et("abort-on-property-read");
                o(),
                    Lt("abort-on-property-read", window, e, r, n),
                    i()
            },
            "abort-on-property-write": function (e, t) {
                const n = Xe(arguments)
                    , { mark: r, end: o } = et("abort-on-property-write")
                    , i = !("false" === t);
                r(),
                    Nt("abort-on-property-write", window, e, n, i),
                    o()
            },
            "array-override": function (e, t, n = "false", r, i) {
                if (!e)
                    throw new Bt("[array-override snippet]: Missing method to override.");
                if (!t)
                    throw new Bt("[array-override snippet]: Missing needle.");
                Xt || (Xt = new zt);
                let s = at("array-override");
                const { mark: a, end: c } = et("array-override")
                    , u = Xe(arguments);
                if ("push" !== e || Xt.has("push"))
                    if ("includes" !== e || Xt.has("includes")) {
                        if ("forEach" === e && !Xt.has("forEach")) {
                            a();
                            const { forEach: e } = Ut.prototype;
                            Xt.set("forEach", Te([]));
                            let t = l(e, (function (t, n) {
                                const r = Xt.get("forEach");
                                return o(e, this, [function (e, i, a) {
                                    for (const { needleRegex: t, pathSegments: n, stackNeedles: o, formattedArgs: i } of r)
                                        if (n.length || "string" != typeof e && "number" != typeof e) {
                                            if (n.length && "object" == typeof e && null !== e && Qt(e, t, n) && qt(o, s))
                                                return s("success", `Array.forEach skipped callback for object containing needle: ${t}\nFILTER: array-override ${i}`),
                                                    void Kt("array-override " + i)
                                        } else {
                                            const n = e.toString();
                                            if (n.match && n.match(t) && qt(o, s))
                                                return s("success", `Array.forEach skipped callback for item matching needle: ${t}\nFILTER: array-override ${i}`),
                                                    void Kt("array-override " + i)
                                        }
                                    return o(t, n || this, [e, i, a])
                                }
                                    , n])
                            }
                            ));
                            dt(t, e),
                                Vt.defineProperty(window.Array.prototype, "forEach", {
                                    value: t
                                }),
                                s("info", "Wrapped Array.prototype.forEach"),
                                c()
                        }
                    } else {
                        a();
                        const { includes: e } = Ut.prototype;
                        Xt.set("includes", Te([]));
                        let t = l(e, (function (t) {
                            const n = Xt.get("includes");
                            for (const { needleRegex: e, retVal: r, pathSegments: o, stackNeedles: i, formattedArgs: a } of n)
                                if (o.length || "string" != typeof t && "number" != typeof t) {
                                    if (o.length && "object" == typeof t && null !== t && Qt(t, e, o) && qt(i, s))
                                        return s("success", `Array.includes returned ${r} for object containing ${e}\nFILTER: array-override ${a}`),
                                            Kt("array-override " + a),
                                            r
                                } else if (t.toString().match && t.toString().match(e) && qt(i, s))
                                    return s("success", `Array.includes returned ${r} for ${e}\nFILTER: array-override ${a}`),
                                        Kt("array-override " + a),
                                        r;
                            return o(e, this, arguments)
                        }
                        ));
                        dt(t, e),
                            Vt.defineProperty(window.Array.prototype, "includes", {
                                value: t
                            }),
                            s("info", "Wrapped Array.prototype.includes"),
                            c()
                    }
                else {
                    a();
                    const { push: e } = Ut.prototype;
                    Xt.set("push", Te([]));
                    let t = l(e, (function (t) {
                        const n = Xt.get("push");
                        for (const { needleRegex: e, pathSegments: r, stackNeedles: o, formattedArgs: i } of n)
                            if (r.length || "string" != typeof t && "number" != typeof t) {
                                if (r.length && "object" == typeof t && null !== t && Qt(t, e, r) && qt(o, s))
                                    return s("success", `Array.push is ignored for object containing needle: ${e}\nFILTER: array-override ${i}`),
                                        void Kt("array-override " + i)
                            } else {
                                const n = t.toString();
                                if (n.match && n.match(e) && qt(o, s))
                                    return s("success", `Array.push is ignored for needle: ${e}\nFILTER: array-override ${i}`),
                                        void Kt("array-override " + i)
                            }
                        return o(e, this, arguments)
                    }
                    ));
                    dt(t, e),
                        Vt.defineProperty(window.Array.prototype, "push", {
                            value: t
                        }),
                        s("info", "Wrapped Array.prototype.push"),
                        c()
                }
                const f = qe(t, s);
                let p = [];
                r && (p = r.split("."));
                let d = [];
                i && (d = i.split(",").map((e => e.trim())));
                const h = Xt.get(e)
                    , y = "true" === n;
                h.push({
                    needleRegex: f,
                    retVal: y,
                    pathSegments: p,
                    stackNeedles: d,
                    formattedArgs: u
                }),
                    Xt.set(e, h)
            },
            "blob-override": function (e, t = "", n = null) {
                if (!e)
                    throw new en("[blob-override snippet]: Missing parameter search.");
                const r = at("blob-override")
                    , o = Xe(arguments)
                    , { mark: i, end: s } = et("blob-override");
                if (i(),
                    rn.push({
                        match: He(e, r),
                        replaceWith: t,
                        needle: n ? qe(n, r) : null,
                        formattedArgs: o
                    }),
                    rn.length > 1)
                    return;
                const a = Zt;
                function c(e, t = {}) {
                    if (Yt.isArray(e)) {
                        let t = Te(e).join("");
                        for (const e of Te(rn))
                            if ((!e.needle || We(e.needle, t)) && We(e.match, t)) {
                                t = t.replace(e.match, e.replaceWith),
                                    r("success", `Replaced: ${e.match} → ${e.replaceWith},\nFILTER: blob-override ${e.formattedArgs}`);
                                const n = "blob-override " + e.formattedArgs;
                                on.has(n) || (on.add(n),
                                    Ue(n))
                            }
                        e = [t]
                    }
                    const n = nn.construct(a, [e, t]);
                    return tn.setPrototypeOf(n, c.prototype),
                        n
                }
                c.prototype = a.prototype,
                    tn.setPrototypeOf(c, a),
                    dt(c, window.Blob),
                    window.Blob = c,
                    r("info", "Wrapped Blob constructor in context "),
                    s()
            },
            "cookie-remover": function (e, t = !1) {
                if (!e)
                    throw new sn("[cookie-remover snippet]: No cookie to remove.");
                const n = Xe(arguments);
                let r = at("cookie-remover");
                const { mark: o, end: i } = et("cookie-remover");
                let s = qe(e, r)
                    , a = !1;
                if (!Te(/^http|^about/).test(location.protocol))
                    return void r("warn", "Snippet only works for http or https and about.");
                function c() {
                    return Te(cn()).split(";").filter((e => We(s, Te(e).split("=")[0])))
                }
                const l = () => {
                    r("info", "Parsing cookies for matches"),
                        o();
                    for (const e of Te(c())) {
                        let t = Te(location.hostname);
                        !t && Te(location.ancestorOrigins) && Te(location.ancestorOrigins[0]) && (t = new an(Te(location.ancestorOrigins[0])).hostname);
                        const o = Te(e).split("=")[0]
                            , i = "expires=Thu, 01 Jan 1970 00:00:00 GMT"
                            , s = "path=/"
                            , c = t.split(".");
                        for (let e = c.length; e > 0; e--) {
                            const t = c.slice(c.length - e).join(".");
                            cn(`${Te(o).trim()}=;${i};${s};domain=${t}`),
                                cn(`${Te(o).trim()}=;${i};${s};domain=.${t}`),
                                r("success", `Set expiration date on ${o}`, "\nFILTER: cookie-remover", n),
                                a || (a = !0,
                                    Ue("cookie-remover " + n))
                        }
                    }
                    i()
                }
                    ;
                if (l(),
                    t) {
                    let e = c();
                    setInterval((() => {
                        let t = c();
                        if (t !== e)
                            try {
                                l()
                            } finally {
                                e = t
                            }
                    }
                    ), 1e3)
                }
            },
            debug: function (e) {
                tt = !0,
                    e && (nt = qe(e))
            },
            "event-override": function (e, t, n = null, r = null, i = null, a = "") {
                const c = Xe(arguments);
                let u = at("[event-override]");
                if (!("rewrite" !== t || r && i))
                    return void u("error", `rewrite mode requires the property and pattern params.\nFILTER: event-override ${c}`);
                const f = {
                    eventType: e,
                    mode: t,
                    needle: n ? qe(n, u) : null,
                    property: r,
                    search: "rewrite" === t ? He(i, u) : null,
                    replacement: a,
                    formattedArgs: c
                };
                if (gn.includes(f) || gn.push(f),
                    gn.length > 1)
                    return;
                const { mark: p, end: d } = et("event-override")
                    , h = un.getOwnPropertyDescriptor(window.EventTarget.prototype, "addEventListener");
                if (h.configurable) {
                    let e = l(dn, (function (e, t, n) {
                        p();
                        const r = gn.filter((t => t.eventType === e));
                        if (!r.length || e !== r[0].eventType)
                            return d(),
                                o(dn, this, arguments);
                        const i = r.find((e => "disable" === e.mode && (!e.needle || We(e.needle, t.toString()))));
                        if (i)
                            return u("success", `Disabling ${i.eventType} event, \nFILTER: event-override ${i.formattedArgs}`),
                                wn("event-override " + i.formattedArgs),
                                void d();
                        const a = r.filter((e => "trusted" === e.mode && (!e.needle || We(e.needle, t.toString()))))
                            , c = r.filter((e => "rewrite" === e.mode && (!e.needle || We(e.needle, t.toString()))));
                        if (!("function" == typeof t || t && "function" == typeof t.handleEvent) || !a.length && !c.length)
                            return d(),
                                o(dn, this, arguments);
                        const l = function (e) {
                            const n = new Proxy(e, {
                                get(t, n) {
                                    if ("isTrusted" === n && a.length)
                                        return u("success", `Providing trusted value for ${e.type} event`),
                                            wn("event-override " + a[0].formattedArgs),
                                            !0;
                                    const r = fn.get(t, n);
                                    if (c.length && "string" == typeof r) {
                                        const t = c.find((e => e.property === n));
                                        if (t) {
                                            const o = Te(r).replace(t.search, t.replacement).toString();
                                            if (o !== r)
                                                return u("success", `Rewriting ${n} of ${e.type} event, \nFILTER: event-override ${t.formattedArgs}`),
                                                    wn("event-override " + t.formattedArgs),
                                                    o
                                        }
                                    }
                                    return "function" == typeof r ? function (...e) {
                                        return o(r, t, e)
                                    }
                                        : r
                                }
                            });
                            return "function" == typeof t ? s(t, this, n) : s(t.handleEvent, t, n)
                        };
                        return l.originalListener = t,
                            yn.has(t) || yn.set(t, new ln),
                            yn.get(t).set(e, l),
                            u("info", `\nWrapping event listener for ${e}`),
                            d(),
                            o(dn, this, [e, l, n])
                    }
                    ));
                    dt(e, dn),
                        un.defineProperty(window.EventTarget.prototype, "addEventListener", {
                            ...h,
                            value: e
                        })
                }
                const y = un.getOwnPropertyDescriptor(window.EventTarget.prototype, "removeEventListener");
                if (y.configurable) {
                    let e = l(hn, (function (e, t, n) {
                        if (t && yn.has(t) && yn.get(t).has(e)) {
                            const r = yn.get(t).get(e);
                            return yn.get(t).delete(e),
                                o(hn, this, [e, r, n])
                        }
                        return o(hn, this, arguments)
                    }
                    ));
                    dt(e, hn),
                        un.defineProperty(window.EventTarget.prototype, "removeEventListener", {
                            ...y,
                            value: e
                        })
                }
                u("info", "Initialized event-override snippet")
            },
            "freeze-element": function (e, t = "", ...n) {
                const r = Xe(arguments);
                let i, a, c = !1, l = !1, u = Te(n).filter((e => !y(e))), f = Te(n).filter((e => y(e))).map(qe), p = ze(), d = Jn(e);
                !function () {
                    let n = Te(t).split("+");
                    1 === n.length && "" === n[0] && (n = []);
                    for (let t of n)
                        switch (t) {
                            case "subtree":
                                c = !0;
                                break;
                            case "abort":
                                l = !0;
                                break;
                            default:
                                throw new Qn("[freeze] Unknown option passed to the snippet. [selector]: " + e + " [option]: " + t)
                        }
                }();
                let h = {
                    selector: e,
                    shouldAbort: l,
                    rid: p,
                    exceptionSelectors: u,
                    regexExceptions: f,
                    changeId: 0
                };
                function y(e) {
                    return e.length >= 2 && "/" == e[0] && "/" == e[e.length - 1]
                }
                function g() {
                    a = d(),
                        m(a, !1)
                }
                function m(e, t = !0) {
                    for (let n of e)
                        Gn.frozen.has(n) || (Gn.frozen.set(n, h),
                            !t && c && new Yn((e => {
                                for (let t of Te(e))
                                    m(Te(t, "MutationRecord").addedNodes)
                            }
                            )).observe(n, {
                                childList: !0,
                                subtree: !0
                            }),
                            c && Te(n).nodeType === _n && m(Te(n).childNodes))
                }
                function w(e, ...t) {
                    st(`[freeze][${e}] `, ...t)
                }
                function v(e, t, n, r) {
                    let o = r.selector
                        , i = r.changeId
                        , s = "string" == typeof e
                        , a = r.shouldAbort ? "aborting" : "watching";
                    switch (Xn.groupCollapsed(`[freeze][${i}] ${a}: ${o}`),
                    n) {
                        case "appendChild":
                        case "append":
                        case "prepend":
                        case "insertBefore":
                        case "replaceChild":
                        case "insertAdjacentElement":
                        case "insertAdjacentHTML":
                        case "insertAdjacentText":
                        case "innerHTML":
                        case "outerHTML":
                            w(i, s ? "text: " : "node: ", e),
                                w(i, "added to node: ", t);
                            break;
                        case "replaceWith":
                        case "after":
                        case "before":
                            w(i, s ? "text: " : "node: ", e),
                                w(i, "added to node: ", Te(t).parentNode);
                            break;
                        case "textContent":
                        case "innerText":
                        case "nodeValue":
                            w(i, "content of node: ", t),
                                w(i, "changed to: ", e)
                    }
                    w(i, `using the function "${n}"`),
                        Xn.groupEnd(),
                        r.changeId++
                }
                function b(e, t) {
                    if (t)
                        for (let n of t)
                            if (We(n, e))
                                return !0;
                    return !1
                }
                Gn.frozen.has(document) || (Gn.frozen.set(document, !0),
                    function () {
                        let e;
                        function t(e) {
                            return e && Gn.frozen.has(e)
                        }
                        function n(e) {
                            try {
                                return e && (Gn.frozen.has(e) || Gn.frozen.has(Te(e).parentNode))
                            } catch (e) {
                                return !1
                            }
                        }
                        function r(e, t) {
                            try {
                                return e && (Gn.frozen.has(e) && t || Gn.frozen.has(Te(e).parentNode) && !t)
                            } catch (e) {
                                return !1
                            }
                        }
                        function o(e) {
                            return Gn.frozen.get(e)
                        }
                        function i(e) {
                            try {
                                if (Gn.frozen.has(e))
                                    return Gn.frozen.get(e);
                                let t = Te(e).parentNode;
                                return Gn.frozen.get(t)
                            } catch (e) { }
                        }
                        function s(e, t) {
                            try {
                                if (Gn.frozen.has(e) && t)
                                    return Gn.frozen.get(e);
                                let n = Te(e).parentNode;
                                return Gn.frozen.get(n)
                            } catch (e) { }
                        }
                        e = P(Vn, "appendChild", t, o),
                            jt(Vn, "appendChild", e),
                            e = P(Vn, "insertBefore", t, o),
                            jt(Vn, "insertBefore", e),
                            e = P(Vn, "replaceChild", t, o),
                            jt(Vn, "replaceChild", e),
                            e = k(Un, "append", t, o),
                            jt(Un, "append", e),
                            e = k(Un, "prepend", t, o),
                            jt(Un, "prepend", e),
                            e = k(Un, "replaceWith", n, i),
                            jt(Un, "replaceWith", e),
                            e = k(Un, "after", n, i),
                            jt(Un, "after", e),
                            e = k(Un, "before", n, i),
                            jt(Un, "before", e),
                            e = O(Un, "insertAdjacentElement", r, s),
                            jt(Un, "insertAdjacentElement", e),
                            e = O(Un, "insertAdjacentHTML", r, s),
                            jt(Un, "insertAdjacentHTML", e),
                            e = O(Un, "insertAdjacentText", r, s),
                            jt(Un, "insertAdjacentText", e),
                            e = T(Un, "innerHTML", t, o),
                            jt(Un, "innerHTML", e),
                            e = T(Un, "outerHTML", n, i),
                            jt(Un, "outerHTML", e),
                            e = j(Vn, "textContent", t, o),
                            jt(Vn, "textContent", e),
                            e = j(zn, "innerText", t, o),
                            jt(zn, "innerText", e),
                            e = j(Vn, "nodeValue", t, o),
                            jt(Vn, "nodeValue", e)
                    }()),
                    i = new Yn(g),
                    i.observe(document, {
                        childList: !0,
                        subtree: !0
                    }),
                    g();
                let E = !1;
                function $(e) {
                    throw E || (E = !0,
                        Ue("freeze-element " + r)),
                    new er(e)
                }
                function S(e, t, n, r) {
                    let o = new Kn
                        , { body: i } = Te(o.parseFromString(e, "text/html"))
                        , s = R(Te(i).childNodes, t, n, r);
                    return Te(s).map((e => {
                        switch (Te(e).nodeType) {
                            case _n:
                                return Te(e).outerHTML;
                            case Bn:
                                return Te(e).textContent;
                            default:
                                return ""
                        }
                    }
                    )).join("")
                }
                function R(e, t, n, r) {
                    let o = Te([]);
                    for (let i of e)
                        x(i, t, n, r) && o.push(i);
                    return o
                }
                function x(e, t, n, r) {
                    let o = r.shouldAbort
                        , i = r.regexExceptions
                        , s = r.exceptionSelectors
                        , a = r.rid;
                    if ("string" == typeof e) {
                        let s = e;
                        return !!b(s, i) || (rt() && v(s, t, n, r),
                            o && $(a),
                            rt())
                    }
                    let c = e;
                    switch (Te(c).nodeType) {
                        case _n:
                            return !!function (e, t) {
                                if (t) {
                                    let n = Te(e);
                                    for (let e of t)
                                        if (n.matches(e))
                                            return !0
                                }
                                return !1
                            }(c, s) || (o && (rt() && v(c, t, n, r),
                                $(a)),
                                !!rt() && (Hn(c),
                                    v(c, t, n, r),
                                    !0));
                        case Bn:
                            return !!b(Te(c).textContent, i) || (rt() && v(c, t, n, r),
                                o && $(a),
                                !1);
                        default:
                            return !0
                    }
                }
                function P(e, t, n, r) {
                    let i = tr(e, t) || {}
                        , a = i.get && s(i.get, e) || i.value;
                    if (a)
                        return {
                            get: () => function (...e) {
                                if (n(this)) {
                                    let n = r(this);
                                    if (n) {
                                        let r = e[0];
                                        if (!x(r, this, t, n))
                                            return r
                                    }
                                }
                                return o(a, this, e)
                            }
                        }
                }
                function k(e, t, n, r) {
                    let i = tr(e, t) || {}
                        , a = i.get && s(i.get, e) || i.value;
                    if (a)
                        return {
                            get: () => function (...e) {
                                if (!n(this))
                                    return o(a, this, e);
                                let i = r(this);
                                if (!i)
                                    return o(a, this, e);
                                let s = R(e, this, t, i);
                                return s.length > 0 ? o(a, this, s) : void 0
                            }
                        }
                }
                function O(e, t, n, r) {
                    let i = tr(e, t) || {}
                        , a = i.get && s(i.get, e) || i.value;
                    if (a)
                        return {
                            get: () => function (...e) {
                                let [i, c] = e
                                    , l = "afterbegin" === i || "beforeend" === i;
                                if (n(this, l)) {
                                    let e = r(this, l);
                                    if (e) {
                                        let n, r = l ? this : Te(this).parentNode;
                                        switch (t) {
                                            case "insertAdjacentElement":
                                                if (!x(c, r, t, e))
                                                    return c;
                                                break;
                                            case "insertAdjacentHTML":
                                                return n = S(c, r, t, e),
                                                    n ? s(a, this, i, n) : void 0;
                                            case "insertAdjacentText":
                                                if (!x(c, r, t, e))
                                                    return
                                        }
                                    }
                                }
                                return o(a, this, e)
                            }
                        }
                }
                function T(e, t, n, r) {
                    let o = tr(e, t) || {}
                        , { set: i } = o;
                    if (i)
                        return {
                            set(e) {
                                if (!n(this))
                                    return s(i, this, e);
                                let o = r(this);
                                if (!o)
                                    return s(i, this, e);
                                let a = S(e, this, t, o);
                                return a ? s(i, this, a) : void 0
                            }
                        }
                }
                function j(e, t, n, r) {
                    let o = tr(e, t) || {}
                        , { set: i } = o;
                    if (i)
                        return {
                            set(e) {
                                if (!n(this))
                                    return s(i, this, e);
                                let o = r(this);
                                return o ? x(e, this, t, o) ? s(i, this, e) : void 0 : s(i, this, e)
                            }
                        }
                }
            },
            "hide-if-canvas-contains": function (e, t = "canvas", n = "", r = "") {
                const i = at("hide-if-canvas-contains")
                    , s = Xe(arguments)
                    , { mark: a, end: c } = et("hide-if-canvas-contains");
                if (!e)
                    return void i("error", "The parameter 'search' is required");
                if (!fr) {
                    a();
                    const f = nr.prototype;
                    function p(e) {
                        const t = f[e];
                        let n = l(t, (function (e, ...n) {
                            const r = this.canvas;
                            if (dr.has(r))
                                return o(t, this, [e, ...n]);
                            const i = ((pr.get(r) || "") + e).slice(-1e4);
                            pr.set(r, i);
                            for (const [e, t] of fr)
                                "data" !== t.mode && We(e, i) && Er(r, t);
                            const s = o(t, this, [e, ...n]);
                            return $r(r),
                                s
                        }
                        ));
                        dt(n, t),
                            sr.defineProperty(window.CanvasRenderingContext2D.prototype, e, {
                                value: n
                            })
                    }
                    function d() {
                        const e = f.clearRect;
                        let t = l(e, (function (...t) {
                            let n = !1
                                , r = !0;
                            for (const { clearRectBehavior: e } of fr.values())
                                "always" === e && (n = !0),
                                    "never" !== e && (r = !1);
                            if (!r) {
                                const [e, r, o, i] = t
                                    , s = e <= 0 && r <= 0 && o >= this.canvas.width && i >= this.canvas.height;
                                (n || s) && pr.delete(this.canvas)
                            }
                            const i = o(e, this, t);
                            return $r(this.canvas),
                                i
                        }
                        ));
                        dt(t, e),
                            sr.defineProperty(window.CanvasRenderingContext2D.prototype, "clearRect", {
                                value: t
                            })
                    }
                    function h() {
                        const e = f.drawImage;
                        let t = l(e, (function (t, ...n) {
                            if (i("info", "drawImage called with arguments:", t, ...n),
                                t && "string" == typeof t.src && t.src)
                                for (const [e, n] of fr)
                                    "data" !== n.mode && We(e, t.src) && Er(this.canvas, n);
                            const r = o(e, this, [t, ...n]);
                            return $r(this.canvas),
                                r
                        }
                        ));
                        dt(t, e),
                            sr.defineProperty(window.CanvasRenderingContext2D.prototype, "drawImage", {
                                value: t
                            })
                    }
                    i("info", "CanvasRenderingContext2D proxied"),
                        p("fillText"),
                        p("strokeText"),
                        d(),
                        h(),
                        fr = new or;
                    new ir((e => {
                        for (let t of Te(e))
                            "childList" === t.type && hr.forEach((e => {
                                const t = Te(e.canvasElement).closest(e.rule.selector);
                                if (t && !yr.has(t)) {
                                    Hn(t),
                                        yr.add(t),
                                        hr.delete(e),
                                        at("hide-if-canvas-contains")("success", "Matched: ", t, `\nFILTER: hide-if-canvas-contains ${e.rule.formattedArguments}`);
                                    const n = "hide-if-canvas-contains " + e.rule.formattedArguments;
                                    gr.has(n) || (gr.add(n),
                                        Ue(n))
                                }
                            }
                            ))
                    }
                    )).observe(rr, {
                        childList: !0,
                        subtree: !0
                    }),
                        c()
                }
                const u = qe(e, i);
                if (fr.set(u, {
                    selector: t,
                    formattedArguments: s,
                    clearRectBehavior: n,
                    mode: r
                }),
                    "data" === r) {
                    mr = !0,
                        function () {
                            if (wr)
                                return;
                            wr = !0;
                            const e = nr.prototype
                                , t = ["fillRect", "strokeRect", "putImageData", "fill", "stroke"];
                            for (const n of t) {
                                const t = e[n];
                                if ("function" != typeof t)
                                    continue;
                                let r = l(t, (function (...e) {
                                    const n = o(t, this, e);
                                    return $r(this.canvas),
                                        n
                                }
                                ));
                                dt(r, t),
                                    sr.defineProperty(window.CanvasRenderingContext2D.prototype, n, {
                                        value: r
                                    })
                            }
                        }();
                    for (const y of Nn("canvas"))
                        $r(y)
                }
            },
            "hide-if-shadow-contains": function (e, t = "*") {
                const n = Xe(arguments);
                let r = `${e}\\${t}`;
                const i = at("hide-if-shadow-contains");
                Lr.has(r) || Lr.set(r, [qe(e, i), t, it, n]);
                const { mark: s, end: a } = et("hide-if-shadow-contains");
                if (!Ir) {
                    Ir = new xr((e => {
                        s();
                        let t = new kr;
                        for (let { target: n } of Te(e)) {
                            let e = Te(n).parentNode;
                            for (; e;)
                                [n, e] = [e, Te(n).parentNode];
                            if (!Ar.has(n) && !t.has(n)) {
                                t.add(n);
                                for (let [e, t, r, o] of Lr.values())
                                    if (We(e, Te(n).textContent)) {
                                        let e = Te(n.host).closest(t);
                                        if (e) {
                                            r(),
                                                Te(n).appendChild(document.createElement("style")).textContent = ":host {display: none !important}",
                                                Hn(e),
                                                Ar.add(n),
                                                i("success", "Hiding: ", e, `\nFILTER: hide-if-shadow-contains ${o}`);
                                            const t = "hide-if-shadow-contains " + o;
                                            Nr.has(t) || (Nr.add(t),
                                                Ue(t))
                                        }
                                        a()
                                    }
                            }
                        }
                    }
                    ));
                    let e = l(jr, (function () {
                        let e = o(jr, this, arguments);
                        return i("info", "attachShadow is called for: ", e),
                            Ir.observe(e, {
                                childList: !0,
                                characterData: !0,
                                subtree: !0
                            }),
                            e
                    }
                    ));
                    dt(e, jr),
                        Pr.defineProperty(Tr, "attachShadow", {
                            value: e
                        })
                }
            },
            "json-override": function (e, t, n = "", r = "") {
                if (!e)
                    throw new Jr("[json-override snippet]: Missing paths to override.");
                if (void 0 === t)
                    throw new Jr("[json-override snippet]: No value to override with.");
                let i = at("json-override");
                const { mark: s, end: a } = et("json-override");
                if (!zr) {
                    function p(e, t) {
                        for (let { formattedArgs: n, prune: r, jsonPathObjects: o, needle: s, filter: a, value: c } of zr.values())
                            if (!a || We(a, t)) {
                                if (Te(s).some((t => !Ct(e, t))))
                                    return e;
                                for (let t of r)
                                    if (t.startsWith("jsonpath("))
                                        try {
                                            const r = o.get(t);
                                            r.evaluate(e).forEach((({ parent: e, key: t }) => {
                                                i("success", `JSONPath match found at [${t}], replaced with ${c}`, `\nFILTER: json-override ${n}`),
                                                    Gr("json-override " + n),
                                                    e[t] = Dt(c)
                                            }
                                            ))
                                        } catch (e) {
                                            i("error", `JSONPath evaluation failed for: ${t}. Error: ${e.message}`)
                                        }
                                    else
                                        t.includes("{}") || t.includes("[]") ? d(e, t, c, n) : h(e, t, c, n)
                            }
                        return e
                    }
                    function d(e, t, n, r) {
                        let o = Te(t).split(".")
                            , s = e;
                        for (let e = 0; e < o.length; e++) {
                            let a = o[e];
                            if ("[]" === a)
                                return void (Hr.isArray(s) && (i("info", `Iterating over array at: ${a}`),
                                    Te(s).forEach((t => {
                                        null != t && d(t, o.slice(e + 1).join("."), n, r)
                                    }
                                    ))));
                            if ("{}" === a)
                                return void (s && "object" == typeof s && (i("info", `Iterating over object at: ${a}`),
                                    Vr.keys(s).forEach((t => {
                                        let i = s[t];
                                        null != i && d(i, o.slice(e + 1).join("."), n, r)
                                    }
                                    ))));
                            if (!s || "object" != typeof s || !w(s, a))
                                return;
                            e === o.length - 1 ? (i("success", `Found ${t}, replaced it with ${n}`, `\nFILTER: json-override ${r}`),
                                Gr("json-override " + r),
                                s[a] = Dt(n)) : s = s[a]
                        }
                    }
                    function h(e, t, n, r) {
                        let o = Ct(e, t);
                        void 0 !== o && (i("success", `Found ${t}, replaced it with ${n}`, `\nFILTER: json-override ${r}`),
                            Gr("json-override " + r),
                            o[0][o[1]] = Dt(n))
                    }
                    s();
                    let { parse: y } = _r;
                    zr = new Br;
                    let g = l(y, (function (e) {
                        return p(o(y, this, arguments), e)
                    }
                    ));
                    dt(g, y),
                        Vr.defineProperty(window.JSON, "parse", {
                            value: g
                        }),
                        i("info", "Wrapped JSON.parse for override");
                    let { json: m } = Ur.prototype;
                    Vr.defineProperty(window.Response.prototype, "json", {
                        value: l(m, (function (e) {
                            return o(m, this, arguments).then((t => p(t, e)))
                        }
                        ))
                    }),
                        i("info", "Wrapped Response.json for override"),
                        a()
                }
                const c = Xe(arguments)
                    , u = Te(e).split(/ +/)
                    , f = new Br;
                for (const v of u)
                    if (v.startsWith("jsonpath("))
                        try {
                            f.set(v, new Wr(v.slice(9, -1)))
                        } catch (b) {
                            i("error", `Invalid JSONPath query: ${v}. Error: ${b.message}`)
                        }
                zr.set(e, {
                    formattedArgs: c,
                    prune: u,
                    jsonPathObjects: f,
                    needle: n.length ? Te(n).split(/ +/) : [],
                    filter: r ? qe(r, i) : null,
                    value: t
                })
            },
            "json-prune": function (e, t = "", n = "") {
                if (!e)
                    throw new Qr("Missing paths to prune");
                let r = at("json-prune");
                const { mark: i, end: s } = et("json-prune");
                if (!no) {
                    function f(e) {
                        for (let { prune: t, needle: n, jsonPathObjects: o, stackNeedle: i, formattedArgs: s } of no.values()) {
                            if (Te(n).length > 0 && Te(n).some((t => !Ct(e, t))))
                                return e;
                            if (Te(i) && Te(i).length > 0 && !qt(i, r))
                                return e;
                            for (let n of t)
                                if (n.startsWith("jsonpath("))
                                    try {
                                        const t = o.get(n);
                                        t.evaluate(e).forEach((({ parent: e, key: t }) => {
                                            r("success", `JSONPath match found and deleted at [${t}]`, `\nFILTER: json-prune ${s}`),
                                                oo("json-prune " + s),
                                                delete e[t]
                                        }
                                        ))
                                    } catch (e) {
                                        r("error", `JSONPath evaluation failed for: ${n}. Error: ${e.message}`)
                                    }
                                else
                                    n.includes("{}") || n.includes("[]") || n.includes("{-}") || n.includes("[-]") ? p(e, n, s) : h(e, n, s)
                        }
                        return e
                    }
                    function p(e, t, n) {
                        let o = Te(t).split(".")
                            , i = e;
                        for (let e = 0; e < o.length; e++) {
                            let s = o[e];
                            if ("[]" === s)
                                return void (Kr.isArray(i) && (r("info", `Iterating over array at: ${s}`),
                                    Te(i).forEach((t => p(t, o.slice(e + 1).join("."), n)))));
                            if ("[-]" === s) {
                                if (Kr.isArray(i)) {
                                    r("info", `Iterating over array with element removal at: ${s}`);
                                    let t = o.slice(e + 1).join(".")
                                        , a = [];
                                    Te(i).forEach(((e, n) => {
                                        d(e, t) && a.push(n)
                                    }
                                    ));
                                    for (let e = a.length - 1; e >= 0; e--)
                                        r("success", `Found element at index ${a[e]} matching ${t} and removed entire element, \nFILTER: json-prune ${n}`),
                                            oo("json-prune " + n),
                                            i.splice(a[e], 1)
                                }
                                return
                            }
                            if ("{}" === s)
                                return void ("object" == typeof i && null !== i && (r("info", `Iterating over object at: ${s}`),
                                    eo.keys(i).forEach((t => p(i[t], o.slice(e + 1).join("."), n)))));
                            if ("{-}" === s) {
                                if ("object" == typeof i && null !== i) {
                                    r("info", `Iterating over object with element removal at: ${s}`);
                                    let t = o.slice(e + 1).join(".")
                                        , a = [];
                                    eo.keys(i).forEach((e => {
                                        d(i[e], t) && a.push(e)
                                    }
                                    )),
                                        a.forEach((e => {
                                            r("success", `Found object key ${e} matching ${t} and removed entire element, \nFILTER: json-prune ${n}`),
                                                oo("json-prune " + n),
                                                delete i[e]
                                        }
                                        ))
                                }
                                return
                            }
                            if (!i || "object" != typeof i || !w(i, s))
                                return;
                            e === o.length - 1 ? (r("success", `Found ${t} and deleted, \nFILTER: json-prune ${n}`),
                                oo("json-prune " + n),
                                delete i[s]) : i = i[s]
                        }
                    }
                    function d(e, t) {
                        if (!t || "" === t)
                            return !0;
                        let n = Te(t).split(".")
                            , r = e;
                        for (let e = 0; e < n.length; e++) {
                            let t = n[e];
                            if ("[]" === t)
                                return !!Kr.isArray(r) && Te(r).some((t => d(t, n.slice(e + 1).join("."))));
                            if ("{}" === t)
                                return "object" == typeof r && null !== r && eo.keys(r).some((t => d(r[t], n.slice(e + 1).join("."))));
                            if (!r || "object" != typeof r || !w(r, t))
                                return !1;
                            if (e === n.length - 1)
                                return !0;
                            r = r[t]
                        }
                        return !1
                    }
                    function h(e, t, n) {
                        let o = Ct(e, t);
                        void 0 !== o && (r("success", `Found ${t} and deleted`, `\nFILTER: json-prune ${n}`),
                            oo("json-prune " + n),
                            delete o[0][o[1]])
                    }
                    i();
                    let { parse: y } = Yr;
                    no = new Zr;
                    let g = l(y, (function () {
                        return f(o(y, this, arguments))
                    }
                    ));
                    dt(g, y),
                        eo.defineProperty(window.JSON, "parse", {
                            value: g
                        }),
                        r("info", "Wrapped JSON.parse for prune");
                    let { json: m } = to.prototype
                        , v = l(m, (function () {
                            return o(m, this, arguments).then((e => f(e)))
                        }
                        ));
                    dt(v, m),
                        eo.defineProperty(window.Response.prototype, "json", {
                            value: v
                        }),
                        r("info", "Wrapped Response.json for prune"),
                        s()
                }
                const a = Xe(arguments)
                    , c = Te(e).split(/ +/)
                    , u = new Zr;
                for (const b of c)
                    if (b.startsWith("jsonpath("))
                        try {
                            u.set(b, new Wr(b.slice(9, -1)))
                        } catch (E) {
                            r("error", `Invalid JSONPath query: ${b}. Error: ${E.message}`)
                        }
                no.set(e, {
                    formattedArgs: a,
                    prune: c,
                    jsonPathObjects: u,
                    needle: t.length ? Te(t).split(/ +/) : [],
                    stackNeedle: n.length ? Te(n).split(/ +/) : []
                })
            },
            "map-override": function (e, t, n = "", r, i) {
                if (!e)
                    throw new io("[map-override snippet]: Missing method to override.");
                if (!t)
                    throw new io("[map-override snippet]: Missing needle.");
                co || (co = new ao);
                let a = at("map-override");
                const { mark: c, end: u } = et("map-override")
                    , { set: f, get: p, has: d } = ao.prototype
                    , h = Xe(arguments);
                if ("set" !== e || co.has("set"))
                    if ("get" !== e || co.has("get")) {
                        if ("has" === e && !co.has("has")) {
                            c(),
                                s(f, co, "has", Te([]));
                            let e = l(d, (function (e) {
                                const t = s(p, co, "has");
                                for (const { needleRegex: n, retVal: r, stackNeedles: o } of t)
                                    if ("string" == typeof e || "number" == typeof e) {
                                        const t = e.toString();
                                        if (We(n, t) && qt(o, a))
                                            return a("success", `Map.has returned ${r} for key: ${t}\nFILTER: map-override ${h}`),
                                                uo("map-override " + h),
                                                r
                                    }
                                return o(d, this, arguments)
                            }
                            ));
                            dt(e, d),
                                so.defineProperty(window.Map.prototype, "has", {
                                    value: e
                                }),
                                a("info", "Wrapped Map.prototype.has"),
                                u()
                        }
                    } else {
                        c(),
                            s(f, co, "get", Te([]));
                        let e = l(p, (function (e) {
                            const t = s(p, co, "get");
                            for (const { needleRegex: n, retVal: r, stackNeedles: o } of t)
                                if ("string" == typeof e || "number" == typeof e) {
                                    const t = e.toString();
                                    if (We(n, t) && qt(o, a))
                                        return a("success", `Map.get returned ${r} for key: ${t}\nFILTER: map-override ${h}`),
                                            uo("map-override " + h),
                                            r
                                }
                            return o(p, this, arguments)
                        }
                        ));
                        dt(e, p),
                            so.defineProperty(window.Map.prototype, "get", {
                                value: e
                            }),
                            a("info", "Wrapped Map.prototype.get"),
                            u()
                    }
                else {
                    c(),
                        s(f, co, "set", Te([]));
                    let e = l(f, (function (e, t) {
                        const n = s(p, co, "set");
                        for (const { needleRegex: e, pathSegments: r, stackNeedles: o } of n)
                            if (fo(t, e, r) && qt(o, a))
                                return a("success", `Map.set is ignored for value matching needle: ${e}\nFILTER: map-override ${h}`),
                                    uo("map-override " + h),
                                    this;
                        return o(f, this, arguments)
                    }
                    ));
                    dt(e, f),
                        so.defineProperty(window.Map.prototype, "set", {
                            value: e
                        }),
                        a("info", "Wrapped Map.prototype.set"),
                        u()
                }
                const y = qe(t, a);
                let g = [];
                r && (g = r.split("."));
                let m = [];
                i && (m = i.split(",").map((e => e.trim())));
                const w = s(p, co, e);
                let v;
                "get" === e ? v = "" === n ? void 0 : n : "has" === e && (v = "true" === n),
                    w.push({
                        needleRegex: y,
                        retVal: v,
                        pathSegments: g,
                        stackNeedles: m
                    }),
                    s(f, co, e, w)
            },
            "override-property-read": function (e, t, n) {
                if (!e)
                    throw new po("[override-property-read snippet]: No property to override.");
                if (void 0 === t)
                    throw new po("[override-property-read snippet]: No value to override with.");
                const r = Xe(arguments);
                let o = at("override-property-read");
                const { mark: i, end: s } = et("override-property-read");
                let a = Dt(t)
                    , c = !1;
                o("info", `Overriding ${e}.`);
                const l = !("false" === n);
                i(),
                    jt(window, e, {
                        get: () => (o("success", `${e} override done.`, "\nFILTER: override-property-read", r),
                            c || (c = !0,
                                Ue("override-property-read " + r)),
                            a),
                        set() { }
                    }, l),
                    s()
            },
            "prevent-element-src-loading": function (e, t) {
                if (!e || "string" != typeof e)
                    throw new go("[prevent-element-src-loading snippet]: tagName param must be a string.");
                if (!t)
                    throw new go("[prevent-element-src-loading snippet]: Missing search parameter.");
                if (e = Te(e).toString().toLowerCase(),
                    !Te(["script", "img", "iframe", "link"]).includes(e))
                    throw new go("[prevent-element-src-loading snippet]: tagName parameter is incorrect.");
                const n = {
                    script: "data:text/javascript;base64,KCk9Pnt9",
                    img: "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==",
                    iframe: "data:text/html;base64,PGRpdj48L2Rpdj4=",
                    link: "data:text/plain;base64,"
                }
                    , r = {
                        script: window.HTMLScriptElement,
                        img: window.HTMLImageElement,
                        iframe: window.HTMLIFrameElement,
                        link: window.HTMLLinkElement
                    }[e]
                    , o = "link" === e ? "href" : "src"
                    , i = "onerror"
                    , s = at("[prevent-element-src-loading snippet]")
                    , a = Xe(arguments)
                    , c = "prevent-element-src-loading " + a
                    , { mark: l, end: u } = et("prevent-element-src-loading");
                l();
                const f = qe(t, s);
                if ($o.push({
                    tagName: e,
                    searchRegex: f
                }),
                    s("info", `Added filter rule\nFILTER: prevent-element-src-loading ${a}`),
                    !Ro.has(e)) {
                    Ro.add(e);
                    const t = {
                        apply: (e, t, r) => {
                            if (!r[0] || !r[1])
                                return wo.apply(e, t, r);
                            const i = t.nodeName.toLowerCase()
                                , a = r[0].toLowerCase()
                                , l = r[1];
                            return a === o && $o.some((e => i === e.tagName && We(e.searchRegex, l))) ? (Eo.add(t),
                                s("success", `Replaced setAttribute for ${a}: ${l} → ${n[i]}`),
                                So.has(c) || (So.add(c),
                                    Ue(c)),
                                wo.apply(e, t, [a, n[i]])) : wo.apply(e, t, r)
                        }
                    };
                    r.prototype.setAttribute = new Proxy(r.prototype.setAttribute, t),
                        s("info", "Wrapped setAttribute function");
                    const i = mo.getOwnPropertyDescriptor(r.prototype, o);
                    if (!i)
                        return;
                    mo.defineProperty(r.prototype, o, {
                        enumerable: !0,
                        configurable: !0,
                        get() {
                            return i.get.call(this)
                        },
                        set(e) {
                            const t = this.nodeName.toLowerCase();
                            $o.some((n => t === n.tagName && We(n.searchRegex, e))) ? (Eo.add(this),
                                s("success", `Replaced in src/href setter ${e} → ${n[t]}`),
                                So.has(c) || (So.add(c),
                                    Ue(c)),
                                i.set.call(this, n[t])) : i.set.call(this, e)
                        }
                    }),
                        s("info", "Wrapped src/href property setter")
                }
                if (1 === $o.length) {
                    const e = mo.getOwnPropertyDescriptor(HTMLElement.prototype, i);
                    if (!e)
                        return;
                    mo.defineProperty(HTMLElement.prototype, i, {
                        enumerable: !0,
                        configurable: !0,
                        get() {
                            return e.get.call(this)
                        },
                        set(t) {
                            Eo.has(this) ? (s("success", `Replaced in onerror setter ${t} → () => {}`),
                                So.has(c) || (So.add(c),
                                    Ue(c)),
                                e.set.call(this, (() => { }
                                ))) : e.set.call(this, t)
                        }
                    }),
                        s("info", "Wrapped onerror property setter");
                    const t = {
                        apply: (e, t, n) => {
                            if (!n[0] || !n[1] || !t)
                                return wo.apply(e, t, n);
                            const r = n[0];
                            return "function" == typeof t.getAttribute && Eo.has(t) && "error" === r ? (s("success", `Replaced error event handler on ${t} with () => {}`),
                                So.has(c) || (So.add(c),
                                    Ue(c)),
                                wo.apply(e, t, [r, () => { }
                                ])) : wo.apply(e, t, n)
                        }
                    };
                    EventTarget.prototype.addEventListener = new Proxy(EventTarget.prototype.addEventListener, t),
                        s("info", "Wrapped addEventListener");
                    (() => {
                        yo("error", (e => {
                            const t = e.target;
                            if (!t || !t.nodeName)
                                return;
                            const n = t.src || t.href
                                , r = t.nodeName.toLowerCase();
                            $o.some((e => r === e.tagName && n && We(e.searchRegex, n))) && (t.onerror = () => { }
                            )
                        }
                        ), !0),
                            s("info", "Added event listener to defuse global errors")
                    }
                    )()
                }
                u()
            },
            "prevent-listener": function (e, t, n) {
                if (!e)
                    throw new xo("[prevent-listener snippet]: No event type.");
                let r = at("[prevent]");
                if (!Lo) {
                    Lo = new Po;
                    const { mark: e, end: t } = et("prevent-listener");
                    let n = l(Ao, (function (n, i) {
                        e();
                        for (let { evt: e, handlers: t, selectors: o, formattedArgs: a } of Lo.values()) {
                            if (!We(e, n))
                                continue;
                            let c = this instanceof Element;
                            for (let l = 0; l < t.length; l++) {
                                const u = t[l]
                                    , f = o[l];
                                if (f && (!c || !Te(this).matches(f)))
                                    continue;
                                if (u) {
                                    const e = function () {
                                        try {
                                            const e = String("function" == typeof i ? i : i.handleEvent);
                                            return We(u, e)
                                        } catch (e) {
                                            return r("error", "Error while trying to stringify listener: ", e),
                                                !1
                                        }
                                    };
                                    if (!function () {
                                        try {
                                            const e = s(To, "function" == typeof i ? i : i.handleEvent);
                                            return We(u, e)
                                        } catch (e) {
                                            return r("error", "Error while trying to stringify listener: ", e),
                                                !1
                                        }
                                    }() && !e())
                                        continue
                                }
                                const p = "prevent-listener " + a;
                                return No.has(p) || (No.add(p),
                                    Ue(p)),
                                    void (rt() && (Oo.groupCollapsed("DEBUG [prevent] was successful", `\nFILTER: prevent-listener ${a}`),
                                        r("success", `type: ${n} matching ${e}`),
                                        r("success", "handler:", i),
                                        u && r("success", `matching ${u}`),
                                        f && r("success", "on element: ", this, ` matching ${f}`),
                                        r("success", "was prevented from being added"),
                                        Oo.groupEnd()))
                            }
                        }
                        return t(),
                            o(Ao, this, arguments)
                    }
                    ));
                    dt(n, Ao),
                        ko.defineProperty(jo, "addEventListener", {
                            value: n
                        }),
                        r("info", "Wrapped addEventListener")
                }
                const i = Xe(arguments);
                Lo.has(e) || Lo.set(e, {
                    evt: qe(e, r),
                    handlers: [],
                    selectors: [],
                    formattedArgs: i
                });
                let { handlers: a, selectors: c } = Lo.get(e);
                a.push(t ? qe(t, r) : null),
                    c.push(n)
            },
            "prevent-window-open": function (e = "", t = "", n = "iframe") {
                if ("" === n && (n = "iframe"),
                    "iframe" !== n && "obj" !== n && "blank" !== n)
                    throw new _o("[prevent-window-open snippet]: decoy must be iframe, obj or blank.");
                let r = !1;
                Te(e).startsWith("!") && (r = !0,
                    e = Te(e).slice(1));
                const i = at("[prevent-window-open]");
                if (Ko.push({
                    regex: qe(e, i),
                    invert: r,
                    hasDelay: "" !== t,
                    autoRemoveAfter: Xo(t) || 0,
                    decoy: n,
                    formattedArgs: Xe(arguments)
                }),
                    Ko.length > 1)
                    return;
                const { mark: s, end: a } = et("prevent-window-open")
                    , c = Vo.getOwnPropertyDescriptor(window, "open");
                if (!c || "function" != typeof c.value || !c.configurable)
                    return void i("warn", "window.open not wrappable, bailing out");
                const u = c.value
                    , f = (e = 0, t = (() => { }
                    )) => {
                        let n = !1;
                        const r = () => {
                            n || (n = !0,
                                t())
                        }
                            ;
                        Go(r, e);
                        const o = {
                            href: "about:blank",
                            assign() { },
                            replace() { },
                            reload() { },
                            toString: () => "about:blank"
                        }
                            , i = {
                                location: o,
                                defaultView: null,
                                cookie: "",
                                open() { },
                                write() { },
                                writeln() { },
                                close() { }
                            }
                            , s = {
                                length: 0,
                                state: null,
                                scrollRestoration: "auto",
                                back() { },
                                forward() { },
                                go() { },
                                pushState() { },
                                replaceState() { }
                            }
                            , a = new Bo
                            , c = Vo.create(Vo.create(null))
                            , l = new Io(c, {
                                get(e, t, c) {
                                    if (qo(e, t))
                                        return Do(e, t, c);
                                    if ("closed" === t)
                                        return n;
                                    if ("close" === t)
                                        return r;
                                    if ("opener" === t)
                                        return window;
                                    if ("frameElement" === t)
                                        return null;
                                    if (t === Mo)
                                        return "Window";
                                    if ("parent" === t || "top" === t || "self" === t || "window" === t || "globalThis" === t || "frames" === t)
                                        return c;
                                    if ("location" === t)
                                        return o;
                                    if ("document" === t)
                                        return i;
                                    if ("history" === t)
                                        return s;
                                    let l;
                                    try {
                                        l = Do(window, t)
                                    } catch (e) {
                                        return
                                    }
                                    if ("function" == typeof l) {
                                        let e = a.get(t);
                                        return e || (e = () => { }
                                            ,
                                            a.set(t, e)),
                                            e
                                    }
                                    return null === l || "object" != typeof l ? l : void 0
                                },
                                set: (e, t, n) => "location" === t || "opener" === t || (!!Zo.has(t) || Ho(e, t, n)),
                                defineProperty: (e, t, n) => !Zo.has(t) && Co(e, t, n),
                                deleteProperty: (e, t) => Fo(e, t),
                                has: (e, t) => Zo.has(t) || Wo(e, t) || Wo(window, t),
                                setPrototypeOf: () => !1,
                                preventExtensions: () => !1
                            });
                        return i.defaultView = l,
                            l
                    }
                    , p = l(u, (function (e) {
                        s();
                        const t = new Jo(arguments.length);
                        for (let e = 0; e < arguments.length; e++)
                            t[e] = arguments[e];
                        const n = t.join(" ");
                        for (let r = 0; r < Ko.length; r++) {
                            const s = Ko[r];
                            if (We(s.regex, n) === s.invert)
                                continue;
                            if (Yo("prevent-window-open " + s.formattedArgs),
                                i("success", `Prevented window.open(${n})`, `\nFILTER: prevent-window-open ${s.formattedArgs}`),
                                a(),
                                !s.hasDelay)
                                return null;
                            if ("blank" === s.decoy) {
                                t[0] = "about:blank";
                                const e = o(u, this, t)
                                    , n = e && e.close;
                                return "function" == typeof n && Go((() => o(n, e, [])), s.autoRemoveAfter),
                                    e
                            }
                            const c = "obj" === s.decoy ? "object" : "iframe"
                                , l = "obj" === s.decoy ? "data" : "src";
                            let p;
                            try {
                                p = Te(zo).createElement(c),
                                    p[l] = null == e ? "about:blank" : e;
                                const { style: t } = Te(p, "HTMLElement")
                                    , n = Te(t, "CSSStyleDeclaration");
                                n.setProperty("height", "1px", "important"),
                                    n.setProperty("position", "fixed", "important"),
                                    n.setProperty("top", "-1px", "important"),
                                    n.setProperty("width", "1px", "important");
                                const r = Te(zo).body || Te(zo).documentElement;
                                Te(r).appendChild(p)
                            } catch (e) {
                                if (p)
                                    try {
                                        Te(p).remove()
                                    } catch (e) { }
                                return f(s.autoRemoveAfter)
                            }
                            return f(s.autoRemoveAfter, (() => Te(p).remove()))
                        }
                        return i("info", `Allowed window.open(${n})`),
                            a(),
                            o(u, this, arguments)
                    }
                    ));
                dt(p, u),
                    Vo.defineProperty(window, "open", {
                        ...c,
                        value: p
                    }),
                    i("info", "Wrapped window.open")
            },
            profile: function () {
                Ze = !1
            },
            "replace-argument": function (e, t, n = "", r = "", i = "") {
                const s = at("[replace-argument snippet]")
                    , a = "replace-argument " + Xe(arguments)
                    , { mark: c, end: u } = et("replace-argument");
                if (!e || "string" != typeof e)
                    return void s("error", `methodPath param must be a string.\nFILTER: ${a}`);
                const f = "" + t;
                if (!/^\d+$/.test(f))
                    return void s("error", `argPosition param must be a non-negative integer.\nFILTER: ${a}`);
                const p = ri(f, 10)
                    , d = Te(e).split(".")
                    , h = d[d.length - 1];
                let y = window;
                for (let e = 0; e < d.length - 1 && null != y; e++)
                    y = y[d[e]];
                if (null == y || "function" != typeof y[h])
                    return void s("warn", `could not resolve ${e}\nFILTER: ${a}`);
                const g = i ? Te(i).split(",").map((e => e.trim())) : []
                    , m = "" === n
                    , w = {
                        argPosition: p,
                        search: m ? null : He(n, s),
                        replacement: r,
                        wholeValue: m,
                        filterStr: a,
                        stackNeedles: g
                    };
                let v = ii.get(e);
                if (v || (v = new ei,
                    ii.set(e, v)),
                    v.push(w),
                    s("info", `Added rule for ${e}\nFILTER: ${a}`),
                    !si.has(e)) {
                    c(),
                        si.add(e);
                    const t = y[h]
                        , n = l(t, (function () {
                            let n = arguments;
                            try {
                                const t = ii.get(e);
                                if (t)
                                    for (const r of t) {
                                        if (arguments.length <= r.argPosition || !qt(r.stackNeedles, s))
                                            continue;
                                        const t = arguments[r.argPosition];
                                        let o;
                                        if (r.wholeValue)
                                            o = Dt(r.replacement);
                                        else {
                                            if (null !== t && "object" == typeof t)
                                                continue;
                                            const e = "" + t;
                                            if (o = Te(e).replace(r.search, r.replacement).toString(),
                                                o === e)
                                                continue
                                        }
                                        const i = ei.from(arguments);
                                        i[r.argPosition] = o,
                                            ai.has(r.filterStr) || (ai.add(r.filterStr),
                                                Ue(r.filterStr)),
                                            s("success", `argument ${r.argPosition} of ${e} replaced\nFILTER: ${r.filterStr}`),
                                            n = i;
                                        break
                                    }
                            } catch (e) {
                                n = arguments
                            }
                            return o(t, this, n)
                        }
                        ));
                    dt(n, t),
                        ni.defineProperty(y, h, {
                            value: n
                        }),
                        s("info", `${e} wrapped`),
                        u()
                }
            },
            "replace-fetch-request": function (e, t = "", n = null, r = "replace") {
                const o = Xe(arguments)
                    , i = at("replace-fetch-request")
                    , { mark: s, end: a } = et("replace-fetch-request");
                if (!e)
                    throw new gi("[replace-fetch-request]: Missing 'search' parameter");
                var c;
                yi || (yi = new wi,
                    i("info", "fetch proxied"),
                    c = (e, { url: t }) => {
                        s();
                        let n = e;
                        for (const [e, { replacement: r, needle: o, matchesNeedle: s, formattedArgs: a, isJsonPath: c, jsonPathEngine: l, mode: u }] of yi) {
                            if (o) {
                                if (!s({
                                    body: n,
                                    url: t
                                }))
                                    continue;
                                i("info", `'${o}' matched fetch request`)
                            }
                            if (c)
                                try {
                                    let t = mi.parse(n);
                                    const o = l.evaluate(t);
                                    if (!o.length)
                                        continue;
                                    if ("remove" === u) {
                                        Ve(o),
                                            i("success", `JSONPath: removed what ${e} matched`, `\nFILTER: replace-fetch-request ${a}`);
                                        const t = "replace-fetch-request " + a;
                                        bi.has(t) || (bi.add(t),
                                            Ue(t))
                                    } else
                                        Te(o).forEach((({ parent: e, key: t }) => {
                                            let n = _e(r);
                                            "append" === u ? Be(e, t, n) || i("info", `append has no meaning for the value at [${t}], replaced it instead`) : e[t] = n,
                                                i("success", `JSONPath [${u}] at [${t}] with ` + r, "\nFILTER: replace-fetch-request " + a);
                                            const o = "replace-fetch-request " + a;
                                            bi.has(o) || (bi.add(o),
                                                Ue(o))
                                        }
                                        ));
                                    n = mi.stringify(t)
                                } catch (e) {
                                    i("info", "JSONPath: skipping non-JSON body or evaluation error: " + e.message)
                                }
                            else {
                                const t = n;
                                if (n = Te(n).replace(e, r).toString(),
                                    t.toString() !== n.toString()) {
                                    i("success", `'${e}' replaced with '${r}' in fetch request body`, "\nFILTER: replace-fetch-request " + a);
                                    const t = "replace-fetch-request " + a;
                                    bi.has(t) || (bi.add(t),
                                        Ue(t))
                                }
                            }
                        }
                        return a(),
                            n
                    }
                    ,
                    di.push(c),
                    hi());
                const l = Je(n, i);
                if (Te(e).startsWith("jsonpath(")) {
                    if ("replace" !== r && "append" !== r && "remove" !== r)
                        return void i("error", `Unknown mode '${r}'. Use replace, append or remove.`);
                    let s;
                    try {
                        const t = Te(e).slice(9, -1).toString();
                        s = new Wr(t)
                    } catch (t) {
                        return void i("error", `Invalid JSONPath query: ${e}. Error: ${t.message}`)
                    }
                    yi.set(e, {
                        replacement: t,
                        needle: n,
                        matchesNeedle: l,
                        formattedArgs: o,
                        isJsonPath: !0,
                        jsonPathEngine: s,
                        mode: r
                    })
                } else {
                    const s = He(e, i);
                    yi.set(s, {
                        replacement: t,
                        needle: n,
                        matchesNeedle: l,
                        formattedArgs: o,
                        isJsonPath: !1,
                        jsonPathEngine: null,
                        mode: r
                    })
                }
            },
            "replace-fetch-response": function (e, t = "", n = null, r = "replace") {
                const o = Xe(arguments)
                    , i = at("replace-fetch-response")
                    , { mark: s, end: a } = et("replace-fetch-response");
                if (!e)
                    return void i("error", "The parameter 'search' is required");
                if (!Ei) {
                    const e = (e, { url: t }) => {
                        s();
                        return Te(e).clone().text().then((n => {
                            let r = Te(n);
                            for (const [e, { replacement: n, needle: o, matchesNeedle: s, formattedArgs: a, isJsonPath: c, jsonPathEngine: l, mode: u }] of Ei) {
                                if (o) {
                                    if (!s({
                                        body: r,
                                        url: t
                                    })) {
                                        i("warn", `'${o}' did not match fetch response`);
                                        continue
                                    }
                                    i("info", `'${o}' matched fetch response`)
                                }
                                if (c) {
                                    let t, o;
                                    try {
                                        t = $i.parse(r.toString()),
                                            o = l.evaluate(t)
                                    } catch (e) {
                                        i("info", "JSONPath: skipping response: " + e.message);
                                        continue
                                    }
                                    if (!o.length)
                                        continue;
                                    const s = _e(n);
                                    "remove" === u ? (Ve(o),
                                        i("success", `JSONPath: removed what ${e} matched`, `\nFILTER: replace-fetch-response ${a}`)) : Te(o).forEach((({ parent: e, key: t }) => {
                                            "append" === u ? Be(e, t, s) || i("info", `append has no meaning for the value at [${t}], replaced it instead`) : e[t] = s,
                                                i("success", `JSONPath [${u}] at [${t}] with ` + n, `\nFILTER: replace-fetch-response ${a}`)
                                        }
                                        )),
                                        r = Te($i.stringify(t));
                                    const c = "replace-fetch-response " + a;
                                    ki.has(c) || (ki.add(c),
                                        Ue(c));
                                    continue
                                }
                                const f = r.toString();
                                if (r = r.replace(e, n),
                                    r.toString() !== f) {
                                    const t = "replace-fetch-response " + a;
                                    ki.has(t) || (ki.add(t),
                                        Ue(t)),
                                        i("success", `'${e}' replaced with '${n}' in fetch response`, `\nFILTER: replace-fetch-response ${a}`)
                                }
                            }
                            if (r.toString() === n.toString())
                                return e;
                            const o = new xi(r.toString(), {
                                status: e.status,
                                statusText: e.statusText,
                                headers: e.headers
                            });
                            return Ri.defineProperties(o, {
                                ok: {
                                    value: e.ok
                                },
                                redirected: {
                                    value: e.redirected
                                },
                                type: {
                                    value: e.type
                                },
                                url: {
                                    value: e.url
                                }
                            }),
                                a(),
                                o
                        }
                        ))
                    }
                        ;
                    Ei = new Si,
                        i("info", "Network API proxied"),
                        c = e,
                        pi.push(c),
                        hi()
                }
                var c;
                const l = Je(n, i);
                if (Te(e).startsWith("jsonpath(")) {
                    if ("replace" !== r && "append" !== r && "remove" !== r)
                        return void i("error", `Unknown mode '${r}'. Use replace, append or remove.`);
                    let s;
                    try {
                        s = new Wr(Te(e).slice(9, -1).toString())
                    } catch (t) {
                        return void i("error", `Invalid JSONPath query: ${e}. Error: ${t.message}`)
                    }
                    Ei.set(e, {
                        replacement: t,
                        needle: n,
                        matchesNeedle: l,
                        formattedArgs: o,
                        isJsonPath: !0,
                        jsonPathEngine: s,
                        mode: r
                    })
                } else {
                    const s = He(e, i);
                    Ei.set(s, {
                        replacement: t,
                        needle: n,
                        matchesNeedle: l,
                        formattedArgs: o,
                        isJsonPath: !1,
                        jsonPathEngine: null,
                        mode: r
                    })
                }
            },
            "replace-outbound-value": function (e, t = "", n = "", r = "", i = "", s = "") {
                if (!e)
                    throw new Oi("[replace-outbound-value snippet]: Missing method path.");
                let a = at("replace-outbound-value");
                const { mark: c, end: u } = et("replace-outbound-value")
                    , f = Xe(arguments);
                let p = !1;
                function d() {
                    p || (p = !0,
                        Ue("replace-outbound-value " + f))
                }
                function h(e, t, n, r) {
                    if ("base64" === r)
                        try {
                            if (function (e) {
                                try {
                                    if ("" === e)
                                        return !1;
                                    const t = ji(e)
                                        , n = Ai(t)
                                        , r = Te(e).replace(/=+$/, "").toString();
                                    return Te(n).replace(/=+$/, "").toString() === r
                                } catch (e) {
                                    return !1
                                }
                            }(e)) {
                                const r = ji(e);
                                a("info", `Decoded base64 content: ${r}`);
                                const o = t ? Te(r).replace(t, n).toString() : r;
                                a("info", o !== r ? `Modified decoded content: ${o}` : "Decoded content was not modified");
                                const i = Ai(o);
                                return a("info", `Re-encoded to base64: ${i}`),
                                    i
                            }
                            a("info", `Content is plain text: ${e}`);
                            const r = t ? Te(e).replace(t, n).toString() : e;
                            a("info", r !== e ? `Modified plain text content: ${r}` : "Plain text content was not modified");
                            const o = Ai(r);
                            return a("info", `Encoded to base64: ${o}`),
                                o
                        } catch (t) {
                            return a("info", `Error processing base64 content: ${t.message}`),
                                e
                        }
                    return t ? Te(e).replace(t, n).toString() : e
                }
                function y(e, t, n, r, o, i) {
                    const s = n ? He(n, a) : null;
                    if (t.length && "object" == typeof e && null !== e) {
                        const c = n ? function (e, t, n, r, o) {
                            if (!t.length)
                                return e;
                            let i = e;
                            for (let n = 0; n < t.length - 1; n++) {
                                if (!i || "object" != typeof i)
                                    return a("info", `Cannot navigate to path: property '${t[n]}' not found`),
                                        e;
                                i = i[t[n]]
                            }
                            const s = t[t.length - 1];
                            if (!i || "object" != typeof i || !(s in i))
                                return a("info", `Target property '${s}' not found at path`),
                                    e;
                            const c = i[s];
                            if ("string" != typeof c)
                                return a("info", "Property at path is not a string: " + typeof c),
                                    e;
                            const l = h(c, n, r, o);
                            if (l !== c) {
                                const n = JSON.parse(JSON.stringify(e));
                                let r = n;
                                for (let e = 0; e < t.length - 1; e++)
                                    r = r[t[e]];
                                return r[s] = l,
                                    a("info", `Replaced value at path '${t.join(".")}': '${c}' -> '${l}'`),
                                    n
                            }
                            return e
                        }(e, t, s, r, o) : e;
                        return c !== e && (a("success", `Replaced outbound value\nFILTER: replace-outbound-value ${i}`),
                            d()),
                            c
                    }
                    if ("string" == typeof e) {
                        n || a("info", `Original text content: ${e}`);
                        const t = n ? h(e, s, r, o) : e;
                        return t !== e && (a("success", `Replaced outbound value: ${t} \nFILTER: replace-outbound-value ${i}`),
                            d()),
                            t
                    }
                    return e
                }
                c();
                const g = function (e, t) {
                    let n = e
                        , r = Te(t).split(".");
                    for (let e = 0; e < r.length - 1; e++) {
                        let t = r[e];
                        if (!n || "object" != typeof n && "function" != typeof n)
                            return {
                                base: n,
                                prop: t,
                                remainingPath: r.slice(e).join("."),
                                success: !1
                            };
                        n = n[t]
                    }
                    return {
                        base: n,
                        prop: r[r.length - 1],
                        success: !0
                    }
                }(window, e);
                if (!g.success)
                    return a("error", `Could not reach the end of the prop chain: ${e}. Remaining path: ${g.remainingPath}`),
                        void u();
                const { base: m, prop: w } = g
                    , v = m[w];
                if (!v || "function" != typeof v)
                    return a("error", `Could not retrieve the method: ${e}`),
                        void u();
                let b = [];
                i && (b = Te(i).split("."));
                let E = [];
                s && (E = Te(s).split(",").map((e => e.trim())));
                let $ = !1
                    , S = l(v, (function () {
                        if ($)
                            return o(v, this, arguments);
                        $ = !0;
                        const e = o(v, this, arguments);
                        if (E.length && !qt(E, a))
                            return $ = !1,
                                e;
                        if (e && "function" == typeof e.then)
                            return a("info", "Method returned a Promise, modifying resolved value"),
                                $ = !1,
                                e.then((e => {
                                    const o = "object" == typeof e ? JSON.stringify(e) : e;
                                    return a("info", `Promise resolved with value: ${o}`),
                                        y(e, b, t, n, r, i)
                                }
                                )).catch((e => {
                                    throw a("info", `Promise rejected: ${e.message}`),
                                    e
                                }
                                ));
                        const s = y(e, b, t, n, r, i);
                        return $ = !1,
                            s
                    }
                    ));
                dt(S, v),
                    Ti.defineProperty(m, w, {
                        value: S
                    }),
                    a("info", `Wrapped ${e}`),
                    u()
            },
            "replace-xhr-request": function (e, t = "", n = null, r = "replace") {
                const o = Xe(arguments)
                    , i = at("replace-xhr-request")
                    , { mark: s, end: a } = et("replace-xhr-request");
                if (!e)
                    throw new Hi("[replace-xhr-request]: Missing 'search' parameter");
                var c;
                Wi || (Wi = new _i,
                    i("info", "XMLHttpRequest proxied"),
                    c = (e, { url: t }) => {
                        s();
                        let n = e;
                        for (const [e, { replacement: r, needle: o, matchesNeedle: s, formattedArgs: a, isJsonPath: c, jsonPathEngine: l, mode: u }] of Wi) {
                            if (o) {
                                if (!s({
                                    body: n,
                                    url: t
                                }))
                                    continue;
                                i("info", `'${o}' matched XHR request`)
                            }
                            if (c)
                                try {
                                    let t = Ji.parse(n);
                                    const o = l.evaluate(t);
                                    if (!o.length)
                                        continue;
                                    if ("remove" === u) {
                                        Ve(o),
                                            i("success", `JSONPath: removed what ${e} matched`, `\nFILTER: replace-xhr-request ${a}`);
                                        const t = "replace-xhr-request " + a;
                                        Vi.has(t) || (Vi.add(t),
                                            Ue(t))
                                    } else
                                        Te(o).forEach((({ parent: e, key: t }) => {
                                            let n = _e(r);
                                            "append" === u ? Be(e, t, n) || i("info", `append has no meaning for the value at [${t}], replaced it instead`) : e[t] = n,
                                                i("success", `JSONPath [${u}] at [${t}] with ` + r, "\nFILTER: replace-xhr-request " + a);
                                            const o = "replace-xhr-request " + a;
                                            Vi.has(o) || (Vi.add(o),
                                                Ue(o))
                                        }
                                        ));
                                    n = Ji.stringify(t)
                                } catch (e) {
                                    i("info", "JSONPath: skipping non-JSON body or evaluation error: " + e.message)
                                }
                            else {
                                const t = n;
                                if (n = Te(n).replace(e, r).toString(),
                                    t.toString() !== n.toString()) {
                                    i("success", `'${e}' replaced with '${r}' in XHR request body`, "\nFILTER: replace-xhr-request " + a);
                                    const t = "replace-xhr-request " + a;
                                    Vi.has(t) || (Vi.add(t),
                                        Ue(t))
                                }
                            }
                        }
                        return a(),
                            n
                    }
                    ,
                    Ci.push(c),
                    qi());
                const l = Je(n, i);
                if (Te(e).startsWith("jsonpath(")) {
                    if ("replace" !== r && "append" !== r && "remove" !== r)
                        return void i("error", `Unknown mode '${r}'. Use replace, append or remove.`);
                    let s;
                    try {
                        const t = Te(e).slice(9, -1).toString();
                        s = new Wr(t)
                    } catch (t) {
                        return void i("error", `Invalid JSONPath query: ${e}. Error: ${t.message}`)
                    }
                    Wi.set(e, {
                        replacement: t,
                        needle: n,
                        matchesNeedle: l,
                        formattedArgs: o,
                        isJsonPath: !0,
                        jsonPathEngine: s,
                        mode: r
                    })
                } else {
                    const s = He(e, i);
                    Wi.set(s, {
                        replacement: t,
                        needle: n,
                        matchesNeedle: l,
                        formattedArgs: o,
                        isJsonPath: !1,
                        jsonPathEngine: null,
                        mode: r
                    })
                }
            },
            "replace-xhr-response": function (e, t = "", n = null, r = "replace") {
                const o = Xe(arguments)
                    , i = at("replace-xhr-response")
                    , { mark: s, end: a } = et("replace-xhr-response");
                if (!e)
                    return void i("error", "The parameter 'pattern' is required");
                var c;
                Ui || (Ui = new Xi,
                    i("info", "XMLHttpRequest proxied"),
                    c = (e, { url: t }) => {
                        s();
                        let n = e;
                        for (const [r, { replacement: o, needle: s, matchesNeedle: a, formattedArgs: c, isJsonPath: l, jsonPathEngine: u, mode: f }] of Ui) {
                            if (s) {
                                if (!a({
                                    body: n,
                                    url: t
                                })) {
                                    i("warn", `'${s}' did not match XHR response`);
                                    continue
                                }
                                i("info", `'${s}' matched XHR response`)
                            }
                            if (l) {
                                let e, t;
                                try {
                                    e = zi.parse(n),
                                        t = u.evaluate(e)
                                } catch (e) {
                                    i("info", "JSONPath: skipping response: " + e.message);
                                    continue
                                }
                                if (!t.length)
                                    continue;
                                const s = Ki(o);
                                "remove" === f ? (Ve(t),
                                    i("success", `JSONPath: removed what ${r} matched`, `\nFILTER: replace-xhr-response ${c}`)) : Te(t).forEach((({ parent: e, key: t }) => {
                                        "append" === f ? Be(e, t, s) || i("info", `append has no meaning for the value at [${t}], replaced it instead`) : e[t] = s,
                                            i("success", `JSONPath [${f}] at [${t}] with ` + o, `\nFILTER: replace-xhr-response ${c}`)
                                    }
                                    )),
                                    n = zi.stringify(e);
                                const a = "replace-xhr-response " + c;
                                Qi.has(a) || (Qi.add(a),
                                    Ue(a))
                            } else if (n = Te(n).replace(r, o).toString(),
                                e.toString() !== n.toString()) {
                                const e = "replace-xhr-response " + c;
                                Qi.has(e) || (Qi.add(e),
                                    Ue(e)),
                                    i("success", `'${r}' replaced with '${o}' in XHR response`, `\nFILTER: replace-xhr-response ${c}`)
                            }
                        }
                        return a(),
                            n.toString()
                    }
                    ,
                    Fi.push(c),
                    qi());
                const l = Je(n, i);
                if (Te(e).startsWith("jsonpath(")) {
                    if ("replace" !== r && "append" !== r && "remove" !== r)
                        return void i("error", `Unknown mode '${r}'. Use replace, append or remove.`);
                    let s;
                    try {
                        const t = Te(e).slice(9, -1).toString();
                        s = new Wr(t)
                    } catch (t) {
                        return void i("error", `Invalid JSONPath query: ${e}. Error: ${t.message}`)
                    }
                    Ui.set(e, {
                        replacement: t,
                        needle: n,
                        matchesNeedle: l,
                        formattedArgs: o,
                        isJsonPath: !0,
                        jsonPathEngine: s,
                        mode: r
                    })
                } else {
                    const s = He(e, i);
                    Ui.set(s, {
                        replacement: t,
                        needle: n,
                        matchesNeedle: l,
                        formattedArgs: o,
                        isJsonPath: !1,
                        jsonPathEngine: null,
                        mode: r
                    })
                }
            },
            "strip-fetch-query-parameter": function (e, t = null) {
                const n = Xe(arguments)
                    , r = at("strip-fetch-query-parameter")
                    , { mark: o, end: i } = et("strip-fetch-query-parameter")
                    , s = e => {
                        o();
                        for (let [t, n] of Yi.entries()) {
                            const { reg: o, args: i } = n;
                            if ((!o || We(o, e)) && es(e.searchParams, t)) {
                                r("success", `${t} has been stripped from url ${e}`, `\nFILTER: strip-fetch-query-parameter ${i}`);
                                const n = "strip-fetch-query-parameter " + i;
                                ts.has(n) || (ts.add(n),
                                    Ue(n)),
                                    Zi(e.searchParams, t)
                            }
                        }
                        i()
                    }
                    ;
                var a;
                Yi || (Yi = new Map,
                    a = s,
                    fi.push(a),
                    hi()),
                    Yi.set(e, {
                        reg: t && qe(t, r),
                        args: n
                    })
            },
            "timer-override": function (e, t = "", n = "", r = ls.BOTH, i = "") {
                if (!e)
                    throw new ns("[timer-override snippet]: Missing required parameter timerValue.");
                if (!rs.values(ls).includes(r))
                    throw new ns("[timer-override snippet]: Invalid mode. Acceptable values are: " + rs.values(ls).join(", "));
                const a = os(e, 10);
                if (is(a))
                    throw new ns("[timer-override snippet]: timerValue must be a number.");
                const c = at("timer-override");
                if (!us) {
                    us = Te([]);
                    const { mark: f, end: p } = et("timer-override");
                    function d(e) {
                        try {
                            return "function" == typeof e ? s(ss, e) : "" + e
                        } catch (e) {
                            return ""
                        }
                    }
                    function h(e, t, n, r, i, s, a) {
                        const l = d(i);
                        for (const u of us) {
                            if (r.indexOf(u.mode) < 0)
                                continue;
                            if (u.needleRegex) {
                                const e = "" + s;
                                if (!We(u.needleRegex, l) && !We(u.needleRegex, e))
                                    continue;
                                c("info", u.needle + " found in " + l)
                            }
                            if (u.stackNeedles.length > 0 && !qt(u.stackNeedles, c))
                                continue;
                            let f = i;
                            const p = u.newDelay;
                            u.isNoop && (f = () => { }
                                ,
                                c("success", "Callback replaced with noop for " + l)),
                                c("success", n + " replaced with " + p + " for " + l);
                            const d = "timer-override " + u.formattedArgs;
                            fs.has(d) || (fs.add(d),
                                Ue(d));
                            const h = Te([f, p]);
                            for (let e = 2; e < a.length; e++)
                                h.push(a[e]);
                            return o(t, e, h)
                        }
                        return null
                    }
                    f();
                    const y = Te([ls.TIMEOUT, ls.BOTH]);
                    let g = l(as, (function (e, t) {
                        const n = h(this, as, "setTimeout", y, e, t, arguments);
                        return null !== n ? n : o(as, this, arguments)
                    }
                    ));
                    dt(g, as),
                        rs.defineProperty(window, "setTimeout", {
                            value: g
                        });
                    const m = Te([ls.INTERVAL, ls.BOTH]);
                    let w = l(cs, (function (e, t) {
                        const n = h(this, cs, "setInterval", m, e, t, arguments);
                        return null !== n ? n : o(cs, this, arguments)
                    }
                    ));
                    dt(w, cs),
                        rs.defineProperty(window, "setInterval", {
                            value: w
                        }),
                        c("info", "timer APIs proxied"),
                        p()
                }
                let u = [];
                i && (u = i.split(/ +/)),
                    us.push({
                        newDelay: a,
                        needle: t,
                        needleRegex: t ? qe(t, c) : null,
                        mode: r,
                        isNoop: "noop" === n,
                        stackNeedles: u,
                        formattedArgs: Xe(arguments)
                    })
            },
            trace: function (...e) {
                o(st, null, e)
            },
            "tmp-yt-buffering-spoof": function (e, t, n, r) {
                if (bs)
                    return;
                bs = !0;
                const { Document: i, HTMLIFrameElement: s, Storage: a } = Te(window)
                    , c = at("tmp-yt-buffering-spoof")
                    , { mark: u, end: f } = et("tmp-yt-buffering-spoof");
                u();
                let p = Es
                    , d = null
                    , h = 0
                    , y = 0
                    , g = 0
                    , m = ""
                    , w = "";
                const v = {
                    gate: 0,
                    preview: 0,
                    excluded: 0
                }
                    , b = {
                        gate: null,
                        preview: null,
                        excluded: null
                    }
                    , E = window.JSON.stringify
                    , $ = window.JSON.parse
                    , S = function (e) {
                        for (let t = 1; t < arguments.length; t++) {
                            if (null == e)
                                return;
                            e = e[arguments[t]]
                        }
                        return e
                    }
                    , R = (e, t, n) => {
                        v[e]++,
                            t !== b[e] && (b[e] = t,
                                c("info", n))
                    }
                    , x = (e, t, n, r, o) => {
                        if (!O.has(e))
                            try {
                                hs.defineProperty(t, n, {
                                    value: r,
                                    writable: !0,
                                    configurable: !0
                                })
                            } catch (e) {
                                c("error", `Failed to install ${o}: ${e}`)
                            }
                    }
                    , P = hs.getOwnPropertyDescriptor(i.prototype, "visibilityState")
                    , k = function (e) {
                        const t = []
                            , n = [];
                        if ("string" != typeof e || 0 === e.length)
                            return {
                                allow: t,
                                deny: n
                            };
                        for (const r of e.split(/\s+/))
                            r && ("!" === r.charAt(0) && r.length > 1 ? n.push(r.slice(1).toLowerCase()) : t.push(r.toLowerCase()));
                        return {
                            allow: t,
                            deny: n
                        }
                    }("string" == typeof r && r.replace(/\s+/g, "").length > 0 ? r : "!homepage !shorts watch")
                    , O = new ys
                    , T = new ys;
                let j = !1;
                const A = [];
                if ("string" == typeof e) {
                    const t = e.split(/[\s,]+/);
                    for (let e = 0; e < t.length; e++) {
                        const n = t[e];
                        if (!n)
                            continue;
                        const r = /^\d+$/.test(n) ? ws(n, 10) : 0
                            , o = n.toLowerCase();
                        r >= 1 && r <= 10 ? O.add(r) : -1 !== Is.indexOf(o) ? T.add(o) : "useractionmute" === o ? j = !0 : A.push(n)
                    }
                }
                A.length > 0 && c("info", "Ignoring unrecognised token(s) in disabledHooks: " + A.join(", "));
                const L = e => T.has(e)
                    , N = (e, t) => {
                        if (null == e)
                            return t;
                        const n = ws(`${e}`, 10);
                        return n >= 0 ? n : t
                    }
                    , I = N(t, 5e3)
                    , M = N(n, 600)
                    , C = M
                    , F = e => -1 !== e.indexOf("youtube.com/tv") || -1 !== e.indexOf("youtube.com/embed/")
                    , D = e => "string" == typeof e && -1 !== e.indexOf("/shorts/")
                    , q = () => {
                        const e = vs.location.href;
                        return F(e) || D(e)
                    }
                    , W = () => q() || !function (e, t) {
                        const n = Ms(e);
                        return -1 === t.deny.indexOf(n) && (0 === t.allow.length || -1 !== t.allow.indexOf(n))
                    }(vs.location.href, k)
                    , H = () => {
                        try {
                            const e = ms.getElementById("movie_player");
                            return e && "function" == typeof e.getPlayerResponse ? e.getPlayerResponse() : null
                        } catch (e) {
                            return null
                        }
                    }
                    , J = e => {
                        const t = e.videoId || e.playerRequest && e.playerRequest.videoId;
                        return "string" == typeof t && t.length > 0 ? t : ""
                    }
                    , _ = e => {
                        const t = [e]
                            , n = e.playerResponse;
                        return n && "object" == typeof n && n.playabilityStatus && t.push(n),
                            t
                    }
                    , B = e => {
                        const t = S(e, "playerConfig", "audioConfig");
                        if (!t || !t.muteOnStart)
                            return;
                        const n = -1 !== vs.location.href.indexOf("/watch")
                            , r = S(e, "playabilityStatus", "miniplayer");
                        if (!n && (!e.cards || r))
                            return;
                        delete t.muteOnStart;
                        const o = e.messages;
                        o && o[0] && o[0].youThereRenderer && delete o[0].youThereRenderer
                    }
                    , V = e => {
                        if (e.playerRequest)
                            return !1;
                        const t = e.params || e.playbackContext && e.playbackContext.params;
                        if ("string" != typeof t || 0 !== t.indexOf("YAHI"))
                            return !1;
                        const n = vs.location.href;
                        if ("watch" !== Ms(n))
                            return !0;
                        const r = function (e, t) {
                            if ("string" != typeof e || 0 === e.length)
                                return "";
                            const n = new RegExp("[?&]" + t + "=([^&#]+)").exec(e);
                            return n ? n[1] : ""
                        }(n, "v")
                            , o = J(e);
                        return Boolean(r && o && r !== o)
                    }
                    , U = (e, t) => {
                        try {
                            if (!e || !t)
                                return;
                            const n = J(e);
                            if ((e => {
                                const t = vs.location.href;
                                if (F(t))
                                    return !0;
                                if (L("shorts"))
                                    return D(t);
                                const n = S(e, "contentPlaybackContext", "currentUrl") || t;
                                return D(n)
                            }
                            )(t)) {
                                return void R("excluded", n, `Excluded: leaving request for ${n || "?"} untouched.`)
                            }
                            if (!L("preview") && V(e)) {
                                return void R("preview", n, `Preview: leaving request for ${n || "?"} untouched.`)
                            }
                            (e => {
                                const t = J(e);
                                t && (null !== d && d !== t && (c("info", `New video ${t} (was ${d}) — reset to ${Es}`),
                                    p = Es),
                                    d = t)
                            }
                            )(e);
                            const r = (e => {
                                const t = J(e)
                                    , n = H()
                                    , r = S(n, "playabilityStatus", "status");
                                if (L("gate"))
                                    return "LOGIN_REQUIRED" === r || "CONTENT_CHECK_REQUIRED" === r ? Ps : p;
                                let o = "";
                                if ((e => {
                                    const t = e.playerRequest || e;
                                    return !0 === t.racyCheckOk || !0 === t.contentCheckOk
                                }
                                )(e))
                                    o = "request acknowledges an age/content check";
                                else if (Ns(r)) {
                                    const e = S(n, "videoDetails", "videoId") || w;
                                    t && e !== t || (o = `player reports ${r}`)
                                }
                                return o ? (R("gate", `${t}|${o}`, `Gate: sending request for ${t || "?"} unmodified (${o}); state stays ${p}.`),
                                    Ps) : p
                            }
                            )(e)
                                , o = S(e, "context", "client", "clientScreen")
                                , i = t => {
                                    e.params = t,
                                        e.playerRequest && (e.playerRequest.params = t),
                                        e.playbackContext && (e.playbackContext.params = t)
                                }
                                , s = n => {
                                    t.contentPlaybackContext && (t.contentPlaybackContext.lactMilliseconds = `${ds.now()}`),
                                        n && (() => {
                                            try {
                                                hs.defineProperty(ms, "visibilityState", {
                                                    get: () => "visible",
                                                    configurable: !0
                                                })
                                            } catch (e) { }
                                        }
                                        )();
                                    const r = S(e, "context", "client", "configInfo");
                                    r && r.appInstallData && delete r.appInstallData,
                                        h++
                                }
                                ;
                            switch (r) {
                                case Es:
                                    if (o === js)
                                        return;
                                    return i(Os),
                                        void s(!0);
                                case $s:
                                    if (o === js)
                                        return;
                                    return i(Ts),
                                        e.playlistId || (e.context.client.clientScreen = js),
                                        void s(!0);
                                case Ss:
                                    {
                                        if (o === js)
                                            return;
                                        const e = t.params;
                                        if ("string" == typeof e && (0 === e.indexOf(Os) || 0 === e.indexOf(Ts)))
                                            return;
                                        return t.adPlaybackContext = {
                                            pyv: !0
                                        },
                                            void s(!1)
                                    }
                                case Rs:
                                    if ("WEB" !== S(e, "context", "client", "clientName"))
                                        return;
                                    return e.context.client.clientScreen = js,
                                        void s(!0);
                                case xs:
                                    return t.adPlaybackContext = {
                                        adType: "AD_TYPE_INSTREAM"
                                    },
                                        void s(!0);
                                default:
                                    (() => {
                                        try {
                                            P && hs.defineProperty(ms, "visibilityState", P)
                                        } catch (e) { }
                                    }
                                    )()
                            }
                        } catch (e) { }
                    }
                    , z = e => {
                        if (!e || !e.context || !e.context.client)
                            return;
                        const t = e.playbackContext;
                        t && void 0 === t.adPlaybackContext && U(e, t);
                        const n = e.playerRequest && e.playerRequest.playbackContext;
                        n && void 0 === n.adPlaybackContext && U(e, n)
                    }
                    , X = e => {
                        if ("string" != typeof e || -1 === e.indexOf('"contentPlaybackContext"') && -1 === e.indexOf('"adSignalsInfo"'))
                            return null;
                        const t = $(e);
                        if (!t || !t.context || !t.context.client)
                            return null;
                        const n = h;
                        return z(t),
                            h === n ? null : E(t)
                    }
                    , G = l(E, (function () {
                        try {
                            const e = arguments[0];
                            e && "object" == typeof e && z(e)
                        } catch (e) { }
                        return o(E, this, arguments)
                    }
                    ));
                dt(G, E),
                    x(1, window.JSON, "stringify", G, "JSON.stringify");
                const K = l($, (function () {
                    if (W() || p === Ps)
                        return o($, this, arguments);
                    let e;
                    try {
                        e = o($, this, arguments)
                    } catch (e) {
                        return o($, this, arguments)
                    }
                    try {
                        if (!e || "object" != typeof e)
                            return e;
                        if (!e.responseContext && !e.playabilityStatus)
                            return e;
                        y++;
                        const t = L("gate")
                            , n = function (e) {
                                if (!e || "object" != typeof e)
                                    return;
                                const t = e.playabilityStatus;
                                if (t && t.status)
                                    return t.status;
                                const n = e.playerResponse && e.playerResponse.playabilityStatus;
                                return n && n.status ? n.status : void 0
                            }(e);
                        if (!t && Ns(n)) {
                            w = function (e) {
                                if (!e || "object" != typeof e)
                                    return "";
                                const t = e.videoDetails && e.videoDetails.videoId;
                                if (t)
                                    return t;
                                const n = e.playerResponse && e.playerResponse.videoDetails;
                                return n && n.videoId || ""
                            }(e) || d || "";
                            const t = `${d}|${n}`;
                            return t !== m && (m = t,
                                c("info", `Response is ${n} — a gate, not a spoof failure; state stays ${p}.`)),
                                e
                        }
                        const r = E(e);
                        let o = !1;
                        for (const e of As)
                            if (-1 !== r.indexOf(e)) {
                                o = !0;
                                break
                            }
                        if (o) {
                            if (!(t && -1 !== r.indexOf("CONTENT_CHECK_REQUIRED")))
                                return (e => {
                                    const t = ks[p] || Ps;
                                    c("info", `State: ${p} → ${t} (${e})`),
                                        p = t
                                }
                                )("response had error marker"),
                                    e
                        }
                        for (const t of _(e))
                            if (p === Es && B(t),
                                p === xs) {
                                const e = S(t, "playerConfig", "granularVariableSpeedConfig");
                                e && (e.maximumPlaybackRate = 200,
                                    e.minimumPlaybackRate = 25)
                            }
                    } catch (e) { }
                    return e
                }
                ));
                dt(K, $),
                    x(2, window.JSON, "parse", K, "JSON.parse");
                const Q = window.TextEncoder.prototype.encode
                    , Y = l(Q, (function () {
                        try {
                            const e = X(arguments[0]);
                            null !== e && (arguments[0] = e)
                        } catch (e) { }
                        return o(Q, this, arguments)
                    }
                    ));
                dt(Y, Q),
                    x(3, window.TextEncoder.prototype, "encode", Y, "TextEncoder.prototype.encode");
                const Z = new Proxy(window.Request, {
                    construct(e, t, n) {
                        try {
                            const e = t[0]
                                , n = t[1];
                            if (-1 !== ("string" == typeof e ? e : e && "string" == typeof e.url ? e.url : "").indexOf("youtubei") && n) {
                                const e = X(n.body);
                                null !== e && (n.body = e)
                            }
                        } catch (e) { }
                        return Reflect.construct(e, t, n)
                    }
                });
                x(4, window, "Request", Z, "Request");
                const ee = window.XMLHttpRequest.prototype.send
                    , te = l(ee, (function () {
                        try {
                            const e = arguments[0]
                                , t = ps.isArray(e)
                                , n = X(t ? e[0] : e);
                            null !== n && (t ? e[0] = n : arguments[0] = n)
                        } catch (e) { }
                        return o(ee, this, arguments)
                    }
                    ));
                dt(te, ee),
                    x(5, window.XMLHttpRequest.prototype, "send", te, "XMLHttpRequest.prototype.send");
                const ne = {
                    apply(e, t, n) {
                        const r = Reflect.apply(e, t, n);
                        try {
                            if (r && r.responseContext)
                                for (const e of _(r))
                                    delete e.adSlots,
                                        delete e.playerAds,
                                        B(e)
                        } catch (e) { }
                        return r
                    }
                }
                    , re = {
                        apply(e, t, n) {
                            try {
                                const e = n[0];
                                if (e && "string" == typeof e.value && -1 !== e.value.indexOf("playerResponse")) {
                                    let t = e.value;
                                    const r = -1 !== vs.location.href.indexOf("/watch")
                                        , o = -1 !== t.indexOf("cards") && -1 === t.indexOf('"miniplayer"');
                                    (r || o) && -1 !== t.indexOf('"muteOnStart":true') && (t = t.replace('"muteOnStart":true', '"muteOnStart":false'),
                                        t = t.replace('"youThereRenderer":', '"no_youThereRenderer":')),
                                        t = t.replace(/"(adSlots|playerAds)":/g, '"no_ads":'),
                                        e.value = t,
                                        n[0] = e
                                }
                            } catch (e) { }
                            return Reflect.apply(e, t, n)
                        }
                    }
                    , oe = window.Promise.prototype.then
                    , ie = l(oe, (function () {
                        if (W())
                            return o(oe, this, arguments);
                        try {
                            const e = arguments[0];
                            if ("function" == typeof e) {
                                const t = e.toString();
                                -1 !== t.indexOf("jspbResponseCtor") ? arguments[0] = new Proxy(e, ne) : -1 !== t.indexOf(".next(") && (arguments[0] = new Proxy(e, re))
                            }
                        } catch (e) { }
                        return o(oe, this, arguments)
                    }
                    ));
                dt(ie, oe),
                    x(6, window.Promise.prototype, "then", ie, "Promise.prototype.then");
                const se = window.Node.prototype.appendChild
                    , ae = l(se, (function () {
                        const e = o(se, this, arguments);
                        if (q())
                            return e;
                        try {
                            e instanceof s && "about:blank" === e.src && e.contentWindow && (e.contentWindow.fetch = vs.fetch,
                                e.contentWindow.Request = vs.Request)
                        } catch (e) { }
                        return e
                    }
                    ));
                dt(ae, se),
                    x(7, window.Node.prototype, "appendChild", ae, "Node.prototype.appendChild");
                let ce = !1
                    , le = 0
                    , ue = !1;
                const fe = new gs
                    , pe = a && a.prototype && a.prototype.getItem
                    , de = [];
                for (const e of ["sessionStorage", "localStorage"])
                    try {
                        vs[e] && de.push(vs[e])
                    } catch (e) { }
                const he = () => {
                    if ("function" != typeof pe)
                        return !1;
                    for (const e of de)
                        try {
                            const t = o(pe, e, ["yt-player-volume"]);
                            if ("string" != typeof t || 0 === t.length)
                                continue;
                            const n = $(t)
                                , r = n && n.data
                                , i = "string" == typeof r ? $(r) : r;
                            if (i && "boolean" == typeof i.muted)
                                return i.muted || 0 === i.volume
                        } catch (e) { }
                    return !1
                }
                    , ye = () => !W()
                    , ge = (e, t, n, r) => {
                        g++;
                        const o = (e => {
                            let t = !1;
                            try {
                                const e = ms.getElementById("movie_player");
                                e && "function" == typeof e.unMute && (e.unMute(),
                                    t = !0,
                                    "function" == typeof e.getVolume && "function" == typeof e.setVolume && 0 === e.getVolume() && e.setVolume(100))
                            } catch (e) { }
                            try {
                                e && e.muted && (e.muted = !1)
                            } catch (e) { }
                            return t
                        }
                        )(e);
                        c("info", `${t} ${n} for videoId=${r} — unmuted (via ` + (o ? "player.unMute()" : "element") + ").")
                    }
                    , me = (e, t, n) => {
                        if ("playing" === n && (t.playingAt = ds.now(),
                            t.lateReverts = 0),
                            !ye())
                            return;
                        if (!e.muted)
                            return void (ce = !1);
                        const r = S(H(), "videoDetails", "videoId") || "?"
                            , o = `[video.${n}]`;
                        if (j && ((e, t, n) => "volumechange" === e && (ue || 0 !== le && ds.now() - le < M) ? (ce = !0,
                            c("info", `${t} mute within user-gesture window — remembering + respecting user mute (videoId=${n}).`),
                            !0) : ("playing" === e && !ce && he() && (ce = !0,
                                c("info", `${t} stored player preference is muted — treating as user mute.`)),
                                !!ce && (c("info", `${t} muted — user mute active, respecting (videoId=${n}).`),
                                    !0)))(n, o, r))
                            return;
                        let i = "muted at first playing";
                        const s = "volumechange" === n;
                        if (s) {
                            if (0 === t.playingAt)
                                return;
                            const e = ds.now() - t.playingAt;
                            if (e >= I || t.lateReverts >= 5)
                                return;
                            i = `late mute at +${e}ms after playing`
                        }
                        const a = () => {
                            s && t.lateReverts++,
                                ge(e, o, i, r)
                        }
                            ;
                        if (j)
                            return void a();
                        const l = () => {
                            !e.muted || !ye() || t.lateReverts >= 5 || (he() ? c("info", `${o} muted — stored preference says the user muted, respecting (videoId=${r}).`) : a())
                        }
                            ;
                        s && C > 0 ? window.setTimeout((() => {
                            try {
                                l()
                            } catch (e) { }
                        }
                        ), C) : l()
                    }
                    , we = () => {
                        if (W())
                            return;
                        const e = (() => {
                            const e = ms.getElementById("movie_player");
                            return e ? e.querySelector("video.html5-main-video") || e.querySelector("video.video-stream") : null
                        }
                        )();
                        if (!e || fe.has(e))
                            return;
                        fe.add(e);
                        const t = {
                            playingAt: 0,
                            lateReverts: 0
                        };
                        for (const n of ["playing", "volumechange"])
                            e.addEventListener(n, (() => {
                                try {
                                    me(e, t, n)
                                } catch (e) { }
                            }
                            ));
                        c("info", `[video-watcher] attached to <video> element (late-mute window=${I}ms).`)
                    }
                    ;
                if (ve = 10,
                    !O.has(ve)) {
                    we(),
                        new MutationObserver(we).observe(ms, {
                            childList: !0,
                            subtree: !0
                        }),
                        ms.addEventListener("yt-navigate-finish", we);
                    const e = () => {
                        const e = () => {
                            le = ds.now()
                        }
                            , t = e => {
                                const t = e.target;
                                return !(!t || "function" != typeof t.closest || !t.closest(".ytp-volume-area, .ytp-mute-button, .ytp-volume-panel"))
                            }
                            , n = (e, t) => {
                                ms.addEventListener(e, (e => {
                                    try {
                                        t(e)
                                    } catch (e) { }
                                }
                                ), !0)
                            }
                            , r = () => {
                                ue && (ue = !1,
                                    e())
                            }
                            ;
                        n("pointerdown", (n => {
                            t(n) ? (ue = !0,
                                e()) : ue = !1
                        }
                        ));
                        for (const e of ["pointerup", "pointercancel"])
                            n(e, r);
                        vs.addEventListener("blur", r, !0);
                        for (const r of ["click", "wheel"])
                            n(r, (n => {
                                t(n) && e()
                            }
                            ));
                        ms.addEventListener("keydown", (t => {
                            try {
                                const n = t.key
                                    , r = ms.activeElement;
                                if ("m" === n || "M" === n) {
                                    const t = r && r.tagName ? r.tagName : "";
                                    if ("INPUT" === t || "TEXTAREA" === t || r && r.isContentEditable)
                                        return;
                                    return void e()
                                }
                                ("ArrowUp" === n || "ArrowDown" === n) && r && "function" == typeof r.closest && r.closest("#movie_player") && e()
                            } catch (e) { }
                        }
                        ), !0)
                    }
                        ;
                    j && e()
                }
                var ve;
                c("info", `Installed. Starting state: ${p}. Hooks: JSON.{stringify,parse}, TextEncoder.encode, Request, XMLHttpRequest.send, Promise.then, Node.appendChild, video-unmute. Counters: ${h} mutations, ${y} responses inspected, ${v.gate} gate skips, ${v.preview} preview skips, ${v.excluded} excluded requests, ${g} video-element unmutes. Windows: late-mute=${I}ms, ` + (j ? `user-gesture=${M}ms (user mute from volume-control gestures).` : `storage-check delay=${C}ms.`) + (O.size > 0 ? ` Disabled hooks: ${[...O].join(",")}.` : "") + (T.size > 0 ? ` Legacy behaviour: ${[...T].join(",")}.` : "") + function (e) {
                    if (0 === e.allow.length && 0 === e.deny.length)
                        return "";
                    const t = [];
                    e.allow.length > 0 && t.push("allow=[" + e.allow.join(",") + "]");
                    e.deny.length > 0 && t.push("deny=[" + e.deny.join(",") + "]");
                    return " Path filter: " + t.join(" ") + "."
                }(k)),
                    f()
            },
            "tmp-yt-force-reload": function (e, t, n, r) {
                if (_s)
                    return;
                _s = !0;
                const o = at("tmp-yt-force-reload")
                    , { mark: i, end: s } = et("tmp-yt-force-reload");
                i();
                const a = (() => {
                    const e = "string" == typeof n ? n.toString() : "0"
                        , t = Ws(e, 10);
                    return isNaN(t) || t < 0 ? 0 : t
                }
                )()
                    , c = "every" === ("string" == typeof e ? e.toString() : "").toLowerCase() ? "every" : "first"
                    , l = (() => {
                        const e = ("string" == typeof t ? t.toString() : "").toLowerCase();
                        return "dom" === e || "player" === e || "both" === e ? e : "none"
                    }
                    )()
                    , u = function (e) {
                        const t = []
                            , n = [];
                        if ("string" != typeof e || 0 === e.length)
                            return {
                                allow: t,
                                deny: n
                            };
                        const r = e.split(/\s+/);
                        for (let e = 0; e < r.length; e++) {
                            const o = r[e];
                            o && ("!" === o.charAt(0) && o.length > 1 ? n.push(o.slice(1).toLowerCase()) : t.push(o.toLowerCase()))
                        }
                        return {
                            allow: t,
                            deny: n
                        }
                    }(r)
                    , f = Cs.now();
                let p = ""
                    , d = 0
                    , h = !1;
                const y = new Ds;
                let g = !1
                    , m = 0;
                const w = () => {
                    if ("none" === l)
                        return;
                    if (-1 === Js.location.href.indexOf("/watch?"))
                        return;
                    if (!Vs(Js.location.href, u))
                        return;
                    const e = qs.getElementById("movie_player");
                    if (!e || "function" != typeof e.loadVideoById)
                        return;
                    if (!(e => {
                        if ("none" === l || !e)
                            return !1;
                        let t = !1
                            , n = !1;
                        if ("dom" === l || "both" === l)
                            try {
                                t = e.classList.contains("ytp-error") || null !== e.querySelector(".ytp-error")
                            } catch (e) { }
                        if ("player" === l || "both" === l)
                            try {
                                const t = "function" == typeof e.getPlayerResponse ? e.getPlayerResponse() : null
                                    , r = t && t.playabilityStatus && t.playabilityStatus.status;
                                n = "string" == typeof r && "OK" !== r && "OK_LIMITED" !== r
                            } catch (e) { }
                        return "both" === l ? t && n : "player" === l ? n : t
                    }
                    )(e))
                        return;
                    let t;
                    try {
                        t = "function" == typeof e.getPlayerResponse ? e.getPlayerResponse() : null
                    } catch (e) {
                        t = null
                    }
                    const n = t && t.videoDetails && t.videoDetails.videoId;
                    if ("string" != typeof n || "" === n)
                        return;
                    if (y.has(n))
                        return;
                    y.add(n);
                    const r = t.playerConfig && t.playerConfig.playbackStartConfig && t.playerConfig.playbackStartConfig.startSeconds || 0;
                    m++;
                    const i = m
                        , s = Cs.now() - f
                        , a = t && t.playabilityStatus && t.playabilityStatus.status;
                    o("info", `error#${i} [+${s}ms] Error detected for "${n}" (signal=${l}, playabilityStatus=${a}). Firing loadVideoById("${n}", ${r}).`);
                    try {
                        e.loadVideoById(n, r)
                    } catch (e) {
                        o("error", `error#${i} loadVideoById threw: ${e}`)
                    }
                }
                    , v = () => {
                        if (h)
                            return !0;
                        if (-1 === Js.location.href.indexOf("/watch?"))
                            return !1;
                        if (!Vs(Js.location.href, u))
                            return !1;
                        const e = qs.getElementById("movie_player");
                        if (!e || "function" != typeof e.loadVideoById)
                            return !1;
                        let t;
                        (e => {
                            if ("none" === l)
                                return;
                            if (g || !e)
                                return;
                            g = !0,
                                new Fs((() => {
                                    w()
                                }
                                )).observe(e, {
                                    attributes: !0,
                                    attributeFilter: ["class"],
                                    childList: !0,
                                    subtree: !0
                                }),
                                o("info", `Error arm attached to movie_player (signal=${l}).`),
                                w()
                        }
                        )(e);
                        try {
                            t = "function" == typeof e.getPlayerResponse ? e.getPlayerResponse() : null
                        } catch (e) {
                            t = null
                        }
                        const n = t && t.videoDetails && t.videoDetails.videoId;
                        if ("string" != typeof n || "" === n)
                            return !1;
                        if (n === p)
                            return !1;
                        const r = Bs(e, "getPlayerState")
                            , i = Bs(e, "getCurrentTime")
                            , s = Bs(e, "getVideoLoadedFraction")
                            , y = Bs(e, "getDuration")
                            , m = Bs(e, "getPlayerStateObject")
                            , v = `state=${r}, current=${i}, loadedFraction=${s}, duration=${y}, isBuffering=${m && m.isBuffering}`;
                        let b, E;
                        if (1 === r || 2 === r || 0 === r ? (b = !1,
                            E = "already playing/paused/ended") : 3 === r && "number" == typeof i && i >= 1 && ("number" == typeof s && s >= .05) ? (b = !1,
                                E = "mid-playback buffer") : (b = !0,
                                    E = "fresh / pre-playback"),
                            !b)
                            return p = n,
                                o("info", `Skipping reload for "${n}": ${E}. ${v}`),
                                !0;
                        const $ = t.playerConfig && t.playerConfig.playbackStartConfig && t.playerConfig.playbackStartConfig.startSeconds || 0;
                        p = n,
                            d++;
                        const S = d
                            , R = n
                            , x = $
                            , P = () => {
                                try {
                                    const t = Cs.now() - f;
                                    o("info", `#${S} [+${t}ms] Firing loadVideoById("${R}", ${x}). ${v}`),
                                        e.loadVideoById(R, x)
                                } catch (e) {
                                    o("error", `#${S} loadVideoById threw: ${e}`)
                                }
                            }
                            ;
                        return a > 0 ? Hs(P, a) : P(),
                            "first" === c && (h = !0,
                                o("info", "first-mode: disabling further reloads after this fire.")),
                            !0
                    }
                    , b = () => {
                        if (v())
                            return;
                        let e = new Fs((() => {
                            v() && e && (e.disconnect(),
                                e = null)
                        }
                        ));
                        e.observe(qs, {
                            childList: !0,
                            subtree: !0
                        }),
                            Hs((() => {
                                e && (e.disconnect(),
                                    e = null)
                            }
                            ), 1e4)
                    }
                    ;
                "loading" === qs.readyState ? qs.addEventListener("DOMContentLoaded", b) : b(),
                    qs.addEventListener("yt-navigate-finish", (() => {
                        let e = 30;
                        const t = () => {
                            v() || (e--,
                                e <= 0 || Hs(t, 100))
                        }
                            ;
                        Hs(t, 100)
                    }
                    )),
                    o("info", "Installed. Mode=" + c + ". " + ("first" === c ? "Fires once on the first video, then disables." : "Fires on every new video (cold load + SPA nav).") + (a > 0 ? ` +${a}ms delay.` : "") + ("none" === l ? " Error arm disabled." : ` Error arm via ${l} signal (1 reload/video).`) + function (e) {
                        if (0 === e.allow.length && 0 === e.deny.length)
                            return "";
                        const t = [];
                        e.allow.length > 0 && t.push("allow=[" + e.allow.join(",") + "]");
                        e.deny.length > 0 && t.push("deny=[" + e.deny.join(",") + "]");
                        return " Path filter: " + t.join(" ") + "."
                    }(u)),
                    s()
            }
        };
        const snippets = Us;
        let context;
        for (const [name, ...args] of filters) {
            if (snippets.hasOwnProperty(name)) {
                try {
                    context = snippets[name].apply(context, args);
                } catch (error) {
                    console.error(error);
                }
            }
        }
        context = void 0;
    }
        ;
    const graph = new Map([["abort-current-inline-script", null], ["abort-on-iframe-property-read", null], ["abort-on-iframe-property-write", null], ["abort-on-property-read", null], ["abort-on-property-write", null], ["array-override", null], ["blob-override", null], ["cookie-remover", null], ["debug", null], ["event-override", null], ["freeze-element", null], ["hide-if-canvas-contains", null], ["hide-if-shadow-contains", null], ["json-override", null], ["json-prune", null], ["map-override", null], ["override-property-read", null], ["prevent-element-src-loading", null], ["prevent-listener", null], ["prevent-window-open", null], ["profile", null], ["replace-argument", null], ["replace-fetch-request", null], ["replace-fetch-response", null], ["replace-outbound-value", null], ["replace-xhr-request", null], ["replace-xhr-response", null], ["strip-fetch-query-parameter", null], ["timer-override", null], ["trace", null], ["tmp-yt-buffering-spoof", null], ["tmp-yt-force-reload", null]]);
    callback.get = snippet => graph.get(snippet);
    callback.has = snippet => graph.has(snippet);
    callback.getGraph = () => graph;
    callback.setEnvironment = env => {
        if (typeof currentEnvironment !== "undefined")
            currentEnvironment = env;
    }
        ;
    callback.setDebugStyle = styles => {
        if (typeof currentEnvironment !== "undefined") {
            delete currentEnvironment.initial;
            currentEnvironment.debugCSSProperties = styles;
        }

    }
        ;
    callback.getEnvironment = () => currentEnvironment;
    /* harmony default export */
    const main = (callback);
    ;// ./src/content/shared/constants.js
    /*
 * This file is part of eyeo's Web Extension Ad Blocking Toolkit (EWE),
 * Copyright (C) 2006-present eyeo GmbH
 *
 * EWE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * EWE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EWE.  If not, see <http://www.gnu.org/licenses/>.
 */

    /**
 * Prefix that should be used for storage and synchronization to avoid conflicts
 * when multiple extensions are installed in the same session.
 *
 * !!! IMPORTANT - DO NOT CHANGE THIS VALUE !!!
 * This exact string "ab" is hardcoded in the build
 * configurations and is replaced during the build process with host-specific
 * values (e.g., "ab" for Adblock, "abp" for Adblock Plus).
 *
 * If you change this value, the build process will NOT replace it, and the
 * extension will fail to work properly due to namespace conflicts.
 *
 * Build configuration references:
 * - host/adblock/build/config/base.mjs (replacements.search)
 * - host/adblockplus/build/webext/config/base.mjs (replacements.search)
 *
 * @type {string}
 */
    const HOST_PREFIX_TO_REPLACE = "ab";

    /**
 * Dataset key used to exchange the communication channel name between content
 * scripts in different contexts (main world and isolated world)
 * @type {string}
 */
    const COMMS_CHANNEL_DATASET_KEY = `${HOST_PREFIX_TO_REPLACE}FiltersChannel`;

    /**
 * Event used to communicate between content script contexts
 * @type {string}
 */
    const HANDSHAKE_EVENT_NAME = `${HOST_PREFIX_TO_REPLACE}-handshake`;

    /**
 * Storage key used to cache the filters config in content scripts
 * @type {string}
 */
    const CACHED_FILTERS_CONFIG_KEY = `${HOST_PREFIX_TO_REPLACE}-filters-config`;

    ;// ./src/all/snippets.js
    /**
 * CSS properties applied to elements hidden in debug mode
 * @type {string[][]}
 */
    const DEBUG_CSS_PROPERTIES = [["background", "repeating-linear-gradient(to bottom, #e67370 0, #e67370 9px, white 9px, white 10px)"], ["outline", "solid red"]];

    ;// ./src/content/main/shims/storage.js
    /*
 * This file is part of eyeo's Web Extension Ad Blocking Toolkit (EWE),
 * Copyright (C) 2006-present eyeo GmbH
 *
 * EWE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * EWE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EWE.  If not, see <http://www.gnu.org/licenses/>.
 */

    /* eslint-disable no-extend-native */

    function shimStorage(CACHED_FILTERS_CONFIG_KEY) {
        // =================== Secured copies of native functions ====================
        // These are captured before page scripts run.
        // Used inside Proxy apply handlers which run after page scripts.
        const { parse: $JSONparse, stringify: $JSONstringify } = JSON;
        const { keys: $ObjectKeys } = Object;
        const { apply: $ReflectApply, ownKeys: $ReflectOwnKeys, get: $ReflectGet, set: $ReflectSet, has: $ReflectHas, getOwnPropertyDescriptor: $ReflectGetOwnPropertyDescriptor, defineProperty: $ReflectDefineProperty, deleteProperty: $ReflectDeleteProperty } = Reflect;
        const { filter: $ArrayFilter } = Array.prototype;
        const { get: $MapGet, set: $MapSet, has: $MapHas } = Map.prototype;
        const $String = String;

        // Helpers using secured copies
        const filter = (arr, fn) => $ReflectApply($ArrayFilter, arr, [fn]);
        const mapGet = (map, key) => $ReflectApply($MapGet, map, [key]);
        const mapSet = (map, key, val) => $ReflectApply($MapSet, map, [key, val]);
        const mapHas = (map, key) => $ReflectApply($MapHas, map, [key]);

        // Need to unwrap our own proxies when multiple extensions run this shim.
        const realLocalStorage = window.localStorage;
        const realSessionStorage = window.sessionStorage;
        let localStorageProxy;
        let sessionStorageProxy;
        function unwrapStorage(storage) {
            if (storage === localStorageProxy) {
                return realLocalStorage;
            }
            if (storage === sessionStorageProxy) {
                return realSessionStorage;
            }
            return storage;
        }

        const originalToStrings = new Map();

        const storageGetItemDesc = Object.getOwnPropertyDescriptor(Storage.prototype, "getItem");
        const originalStorageGetItem = storageGetItemDesc.value;

        // =================== Conditional application of the shim ===================
        function shouldShimStorage() {
            const config = getConfig(window.sessionStorage) || getConfig(window.localStorage);
            return Boolean(config);
        }

        if (!shouldShimStorage()) {
            return;
        }

        // ===================== Storage.prototype.getItem ======================
        // @docs https://developer.mozilla.org/en-US/docs/Web/API/Storage/getItem
        function getConfig(storage) {
            try {
                const configSerialized = $ReflectApply(originalStorageGetItem, unwrapStorage(storage), [CACHED_FILTERS_CONFIG_KEY]);
                if (configSerialized) {
                    return $JSONparse(configSerialized);
                }
            } catch (e) {// If we can't parse, return null
            }
            return null;
        }

        function websiteHasValue(config) {
            return config && typeof config.websiteValue === "string";
        }
        const storageGetItemProxy = new Proxy(originalStorageGetItem, {
            apply(target, thisArg, argumentsList) {
                const key = argumentsList[0];
                const unwrappedThis = unwrapStorage(thisArg);
                if (key === CACHED_FILTERS_CONFIG_KEY) {
                    const config = getConfig(unwrappedThis);
                    if (websiteHasValue(config)) {
                        return config.websiteValue;
                    }
                    return null;
                }
                return $ReflectApply(target, unwrappedThis, argumentsList);
            }
        });
        Object.defineProperty(Storage.prototype, "getItem", {
            ...storageGetItemDesc,
            value: storageGetItemProxy
        });
        mapSet(originalToStrings, storageGetItemProxy, originalStorageGetItem.toString.bind(originalStorageGetItem));

        // ===================== Storage.prototype.setItem ===========================
        // @docs https://developer.mozilla.org/en-US/docs/Web/API/Storage/setItem
        const storageSetItemDesc = Object.getOwnPropertyDescriptor(Storage.prototype, "setItem");
        const originalStorageSetItem = storageSetItemDesc.value;
        const storageSetItemProxy = new Proxy(originalStorageSetItem, {
            apply(target, thisArg, argumentsList) {
                const key = argumentsList[0];
                const unwrappedThis = unwrapStorage(thisArg);
                if (key === CACHED_FILTERS_CONFIG_KEY) {
                    const config = getConfig(unwrappedThis) || {};
                    config.websiteValue = $String(argumentsList[1]);
                    $ReflectApply(target, unwrappedThis, [CACHED_FILTERS_CONFIG_KEY, $JSONstringify(config)]);
                    return void 0;
                }
                return $ReflectApply(target, unwrappedThis, argumentsList);
            }
        });
        Object.defineProperty(Storage.prototype, "setItem", {
            ...storageSetItemDesc,
            value: storageSetItemProxy
        });
        mapSet(originalToStrings, storageSetItemProxy, originalStorageSetItem.toString.bind(originalStorageSetItem));

        // ================== Storage.prototype.removeItem ==========================
        // @docs https://developer.mozilla.org/en-US/docs/Web/API/Storage/removeItem
        const storageRemoveItemDesc = Object.getOwnPropertyDescriptor(Storage.prototype, "removeItem");
        const originalStorageRemoveItem = storageRemoveItemDesc.value;
        const storageRemoveItemProxy = new Proxy(originalStorageRemoveItem, {
            apply(target, thisArg, argumentsList) {
                const key = argumentsList[0];
                const unwrappedThis = unwrapStorage(thisArg);
                if (key === CACHED_FILTERS_CONFIG_KEY) {
                    const config = getConfig(unwrappedThis);
                    if (websiteHasValue(config)) {
                        delete config.websiteValue;
                        $ReflectApply(originalStorageSetItem, unwrappedThis, [CACHED_FILTERS_CONFIG_KEY, $JSONstringify(config)]);
                    }
                    return void 0;
                }
                return $ReflectApply(target, unwrappedThis, argumentsList);
            }
        });
        Object.defineProperty(Storage.prototype, "removeItem", {
            ...storageRemoveItemDesc,
            value: storageRemoveItemProxy
        });
        mapSet(originalToStrings, storageRemoveItemProxy, originalStorageRemoveItem.toString.bind(originalStorageRemoveItem));

        // ==================== Storage.prototype.clear ============================
        // @docs https://developer.mozilla.org/en-US/docs/Web/API/Storage/clear
        const storageClearDesc = Object.getOwnPropertyDescriptor(Storage.prototype, "clear");
        const originalStorageClear = storageClearDesc.value;
        const storageClearProxy = new Proxy(originalStorageClear, {
            apply(target, thisArg, argumentsList) {
                const unwrappedThis = unwrapStorage(thisArg);
                const config = getConfig(unwrappedThis);
                if (config) {
                    delete config.websiteValue;
                }

                $ReflectApply(target, unwrappedThis, argumentsList);

                // Restore our config (without websiteValue)
                if (config && $ObjectKeys(config).length > 0) {
                    $ReflectApply(originalStorageSetItem, unwrappedThis, [CACHED_FILTERS_CONFIG_KEY, $JSONstringify(config)]);
                }
                return void 0;
            }
        });
        Object.defineProperty(Storage.prototype, "clear", {
            ...storageClearDesc,
            value: storageClearProxy
        });
        mapSet(originalToStrings, storageClearProxy, originalStorageClear.toString.bind(originalStorageClear));

        // ===================== Storage.prototype.key ===============================
        // @docs https://developer.mozilla.org/en-US/docs/Web/API/Storage/key
        const storageKeyDesc = Object.getOwnPropertyDescriptor(Storage.prototype, "key");
        const originalStorageKey = storageKeyDesc.value;
        const storageKeyProxy = new Proxy(originalStorageKey, {
            apply(target, thisArg, argumentsList) {
                const unwrappedThis = unwrapStorage(thisArg);
                const config = getConfig(unwrappedThis);
                if (!config || websiteHasValue(config)) {
                    return $ReflectApply(target, unwrappedThis, argumentsList);
                }

                const requestedIndex = argumentsList[0];
                for (let i = 0; i <= requestedIndex; i++) {
                    const key = $ReflectApply(target, unwrappedThis, [i]);
                    if (key === CACHED_FILTERS_CONFIG_KEY) {
                        return $ReflectApply(target, unwrappedThis, [requestedIndex + 1]);
                    }
                }
                return $ReflectApply(target, unwrappedThis, argumentsList);
            }
        });
        Object.defineProperty(Storage.prototype, "key", {
            ...storageKeyDesc,
            value: storageKeyProxy
        });
        mapSet(originalToStrings, storageKeyProxy, originalStorageKey.toString.bind(originalStorageKey));

        // =================== Storage.prototype.length ============================
        // @docs https://developer.mozilla.org/en-US/docs/Web/API/Storage/length
        const storageLengthDesc = Object.getOwnPropertyDescriptor(Storage.prototype, "length");
        const originalStorageLengthGetter = storageLengthDesc.get;
        Object.defineProperty(Storage.prototype, "length", {
            ...storageLengthDesc,
            get() {
                const unwrappedThis = unwrapStorage(this);
                const originalLength = $ReflectApply(originalStorageLengthGetter, unwrappedThis, []);
                const config = getConfig(unwrappedThis);
                if (config && !websiteHasValue(config)) {
                    return originalLength - 1;
                }
                return originalLength;
            }
        });

        // ================== Proxy wrapper for localStorage ===========
        // Handles: {...localStorage}, Object.keys(), Object.values(), for...in, etc.
        const methodProxyCache = new Map();

        function getMethodProxy(method) {
            if (mapHas(methodProxyCache, method)) {
                return mapGet(methodProxyCache, method);
            }
            const methodProxy = new Proxy(method, {
                apply(fn, thisArg, args) {
                    return $ReflectApply(fn, thisArg, args);
                }
            });
            mapSet(methodProxyCache, method, methodProxy);
            // Register toString for the wrapper to preserve function name
            const originalMethod = mapGet(originalToStrings, method);
            if (originalMethod) {
                mapSet(originalToStrings, methodProxy, originalMethod);
            }
            return methodProxy;
        }

        const storageInstanceProxyConfig = {
            ownKeys(target) {
                const keys = $ReflectOwnKeys(target);
                const config = getConfig(target);
                if (config && !websiteHasValue(config)) {
                    return filter(keys, key => key !== CACHED_FILTERS_CONFIG_KEY);
                }
                return keys;
            },

            // Required for spread operator
            getOwnPropertyDescriptor(target, prop) {
                if (prop === CACHED_FILTERS_CONFIG_KEY) {
                    const config = getConfig(target);
                    if (config && !websiteHasValue(config)) {
                        return void 0;
                        // Hide the property entirely
                    }
                    // When website has set a value, return a proper enumerable descriptor
                    // with the website's value (not our internal config)
                    if (websiteHasValue(config)) {
                        return {
                            value: config.websiteValue,
                            writable: true,
                            enumerable: true,
                            configurable: true
                        };
                    }
                }
                return $ReflectGetOwnPropertyDescriptor(target, prop);
            },

            // Needed for 'in' operator
            has(target, prop) {
                if (prop === CACHED_FILTERS_CONFIG_KEY) {
                    const config = getConfig(target);
                    if (config && !websiteHasValue(config)) {
                        return false;
                    }
                }
                return $ReflectHas(target, prop);
            },

            // Forward get/set using original target so native methods work correctly
            get(target, prop) {
                if (prop === CACHED_FILTERS_CONFIG_KEY) {
                    return target.getItem(CACHED_FILTERS_CONFIG_KEY);
                }
                // Return correct toStringTag so Object.prototype.toString returns
                // [object Storage] instead of [object Object] (for older Firefox)
                if (prop === Symbol.toStringTag) {
                    return "Storage";
                }
                const value = $ReflectGet(target, prop, target);
                // For methods, wrap in a proxy to bind `this` to original target
                // while preserving toString behavior
                if (typeof value === "function") {
                    return getMethodProxy(value);
                }
                return value;
            },

            set(target, prop, value) {
                if (prop === CACHED_FILTERS_CONFIG_KEY) {
                    target.setItem(CACHED_FILTERS_CONFIG_KEY, value);
                    return true;
                }
                return $ReflectSet(target, prop, value, target);
            },

            defineProperty(target, prop, descriptor) {
                if (prop === CACHED_FILTERS_CONFIG_KEY) {
                    if ("value" in descriptor) {
                        target.setItem(CACHED_FILTERS_CONFIG_KEY, descriptor.value);
                    }
                    return true;
                }
                return $ReflectDefineProperty(target, prop, descriptor);
            },

            deleteProperty(target, prop) {
                if (prop === CACHED_FILTERS_CONFIG_KEY) {
                    target.removeItem(CACHED_FILTERS_CONFIG_KEY);
                    return true;
                }
                return $ReflectDeleteProperty(target, prop);
            }
        };

        localStorageProxy = new Proxy(window.localStorage, storageInstanceProxyConfig);

        sessionStorageProxy = new Proxy(window.sessionStorage, storageInstanceProxyConfig);

        // Capture the native accessor getters before redefining.
        const localStorageDesc = Object.getOwnPropertyDescriptor(window, "localStorage");
        const sessionStorageDesc = Object.getOwnPropertyDescriptor(window, "sessionStorage");
        const nativeLocalStorageGetter = localStorageDesc && localStorageDesc.get;
        const nativeSessionStorageGetter = sessionStorageDesc && sessionStorageDesc.get;

        function localStorageGetter() {
            return localStorageProxy;
        }
        function sessionStorageGetter() {
            return sessionStorageProxy;
        }

        if (nativeLocalStorageGetter) {
            $ReflectDefineProperty(localStorageGetter, "name", {
                value: nativeLocalStorageGetter.name,
                configurable: true
            });
            mapSet(originalToStrings, localStorageGetter, nativeLocalStorageGetter.toString.bind(nativeLocalStorageGetter));
        }
        if (nativeSessionStorageGetter) {
            $ReflectDefineProperty(sessionStorageGetter, "name", {
                value: nativeSessionStorageGetter.name,
                configurable: true
            });
            mapSet(originalToStrings, sessionStorageGetter, nativeSessionStorageGetter.toString.bind(nativeSessionStorageGetter));
        }

        Object.defineProperty(window, "localStorage", {
            get: localStorageGetter,
            configurable: true,
            enumerable: true
        });

        Object.defineProperty(window, "sessionStorage", {
            get: sessionStorageGetter,
            configurable: true,
            enumerable: true
        });

        // ===================== Function.prototype.toString =========================
        // @docs https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Function/toString
        const functionToStringDesc = Object.getOwnPropertyDescriptor(Function.prototype, "toString");
        const originalFunctionToString = functionToStringDesc.value;
        const functionToStringProxy = new Proxy(originalFunctionToString, {
            apply(target, thisArg, argumentsList) {
                // Call "super" first, just in case the function was overwritten and had
                // checks if it was called
                const r = $ReflectApply(target, thisArg, argumentsList);

                const restoredToString = mapGet(originalToStrings, thisArg);
                if (restoredToString) {
                    return $ReflectApply(restoredToString, thisArg, argumentsList);
                }

                return r;
            }
        });
        Object.defineProperty(Function.prototype, "toString", {
            ...functionToStringDesc,
            value: functionToStringProxy
        });
        mapSet(originalToStrings, functionToStringProxy, originalFunctionToString.toString.bind(originalFunctionToString));
    }

    ;// ./src/content/shared/helpers.js
    /*
 * This file is part of eyeo's Web Extension Ad Blocking Toolkit (EWE),
 * Copyright (C) 2006-present eyeo GmbH
 *
 * EWE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * EWE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EWE.  If not, see <http://www.gnu.org/licenses/>.
 */

    /**
 * Claims a communication channel name from the document's dataset.
 *
 * If a channel name already exists in the dataset, it is consumed (removed
 * from the dataset and returned). If no channel name exists, the fallback
 * channel is stored in the dataset and returned.
 *
 * This mechanism ensures that only one content script can claim the
 * channel name at a time, preventing conflicts when the main world
 * and isolated world scripts execution order is not consistent.
 * @see https://developer.mozilla.org/en-US/docs/Mozilla/Firefox/Releases/139#changes_for_add-on_developers
 * @see https://bugzil.la/1792685
 * @see https://eyeo.atlassian.net/wiki/spaces/B2C/pages/1666678786/Content-script+based+snippets
 *
 * @param {string} fallbackChannel - The channel name to use and store if
 *   none is present.
 * @returns {string} The claimed channel name (either the existing one
 *   or the fallback).
 */
    function claimCommsChannel(fallbackChannel) {
        let channelName = document.documentElement.dataset[COMMS_CHANNEL_DATASET_KEY];

        if (!channelName) {
            channelName = fallbackChannel;
            document.documentElement.dataset[COMMS_CHANNEL_DATASET_KEY] = channelName;
        } else {
            delete document.documentElement.dataset[COMMS_CHANNEL_DATASET_KEY];
        }

        return channelName;
    }

    ;// ./src/all/errors.js
    /*
 * This file is part of eyeo's Web Extension Ad Blocking Toolkit (EWE),
 * Copyright (C) 2006-present eyeo GmbH
 *
 * EWE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * EWE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EWE.  If not, see <http://www.gnu.org/licenses/>.
 */

    const ERROR_NO_CONNECTION = (/* unused pure expression or super */
        null && ("Could not establish connection. " + "Receiving end does not exist."));
    const ERROR_CLOSED_CONNECTION = (/* unused pure expression or super */
        null && ("A listener indicated an asynchronous " + "response by returning true, but the message channel closed before a " + "response was received"));
    // https://bugzilla.mozilla.org/show_bug.cgi?id=1578697
    const ERROR_MANAGER_DISCONNECTED = "Message manager disconnected";

    /**
 * Reconstructs an error from a serializable error object
 *
 * @param {Object} errorData - Error object
 *
 * @returns {Error} error
 */
    function fromSerializableError(errorData) {
        const error = new Error(errorData.message);
        error.cause = errorData.cause;
        error.name = errorData.name;
        error.stack = errorData.stack;

        return error;
    }

    /**
 * Filters out `browser.runtime.sendMessage` errors to do with the receiving end
 * no longer existing.
 *
 * @param {Promise} promise The promise that should have "no connection" errors
 *   ignored. Generally this would be the promise returned by
 *   `browser.runtime.sendMessage`.
 * @return {Promise} The same promise, but will resolve with `undefined` instead
 *   of rejecting if the receiving end no longer exists.
 */
    function ignoreNoConnectionError(promise) {
        return promise.catch(error => {
            if (typeof error == "object" && (error.message == ERROR_NO_CONNECTION || error.message == ERROR_CLOSED_CONNECTION || error.message == ERROR_MANAGER_DISCONNECTED)) {
                return;
            }

            throw error;
        }
        );
    }

    /**
 * Creates serializable error object from given error
 *
 * @param {Error} error - Error
 *
 * @returns {Object} serializable error object
 */
    function toSerializableError(error) {
        return {
            cause: error.cause instanceof Error ? toSerializableError(error.cause) : error.cause,
            message: error.message,
            name: error.name,
            stack: error.stack
        };
    }

    ;// ./src/content/main/snippets.entry.js
    /*
 * This file is part of eyeo's Web Extension Ad Blocking Toolkit (EWE),
 * Copyright (C) 2006-present eyeo GmbH
 *
 * EWE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * EWE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EWE.  If not, see <http://www.gnu.org/licenses/>.
 */

    // Use chrome.storage to detect if we're in an isolated world.
    // Note: chrome.runtime is unreliable since other extensions may expose it
    // in the main world.
    const isMainWorld = !((typeof chrome === "object" && !!chrome.storage) || (typeof browser === "object" && !!browser.storage));

    const nativeDispatch = document.dispatchEvent.bind(document);

    // Get or create a unique channel name for communicating with the isolated world
    const commsChannelName = claimCommsChannel(esm_browser_v4());

    // Creates a sendSnippetHitEvent function that dispatches hit events back to
    // the isolated world via the comms channel. The isolated-world listener
    // receives, validates, and forwards the event to the telemetry pipeline.
    const createMainWorldHitEventSender = (commsChannel, dispatch) => {
        const dispatchFn = dispatch || document.dispatchEvent.bind(document);
        return function sendSnippetHitEvent(filter, domain) {
            try {
                dispatchFn(new CustomEvent(commsChannel, {
                    detail: {
                        type: "ewe:snippet-hit",
                        filter,
                        domain
                    }
                }));
            } catch (e) {// telemetry must never break snippet execution
            }
        }
            ;
    }
        ;

    const runStorageShim = (shimFn, configKey) => {
        try {
            if (typeof shimFn === "function" && configKey) {
                shimFn(configKey);
            }
        } catch (err) {// It would be good to report this error to Sentry, but we don't currently
            // have a way to do that from the main world.
        }
    }
        ;

    const runSnippets = snippetsConfig => {
        const { callback, filters, env, commsChannel, serializeError, dispatchFn } = snippetsConfig;

        if (filters.length) {
            try {
                callback(env, ...filters);
            } catch (e) {
                // It would be good to report this error to Sentry, but we don't currently
                // have a way to do that from the main world.
                const errorEvent = new CustomEvent(commsChannel, {
                    detail: {
                        type: "ewe:main-error",
                        error: serializeError(e)
                    }
                });
                dispatchFn(errorEvent);
            }
        }
    }
        ;

    const createTrustedScriptPolicy = () => {
        const isTrustedTypesSupported = typeof trustedTypes !== "undefined";
        let policy = null;

        try {
            if (isTrustedTypesSupported) {
                policy = trustedTypes.createPolicy(esm_browser_v4(), {
                    createScript: code => code,
                    createScriptURL: url => url
                });
            }
        } catch (_) { }
        return policy;
    }
        ;

    const injectScript = (executable, policy) => {
        const script = document.createElement("script");
        script.type = "application/javascript";
        script.async = false;

        if (policy) {
            script.textContent = policy.createScript(executable);
        } else {
            script.textContent = executable;
        }

        try {
            document.documentElement.appendChild(script);
        } catch (_) { }
        document.documentElement.removeChild(script);
    }
        ;

    const appendSnippets = snippetsConfig => {
        const policy = createTrustedScriptPolicy();
        const { callback, filters, env, shimFn, shimConfigKey, commsChannel, serializeError } = snippetsConfig;

        const snippetsCode = filters.length ? `
    const callback = (${callback});
    const runSnippets = (${runSnippets});
    const serializeError = (${serializeError});
    const createHitSender = (${createMainWorldHitEventSender});
    const env = ${JSON.stringify(env)};
    env.sendSnippetHitEvent = createHitSender(
      "${commsChannel}", null
    );
    const snippetsConfig = {
      callback,
      env,
      filters: ${JSON.stringify(filters)},
      commsChannel: "${commsChannel}",
      serializeError,
      dispatchFn: document.dispatchEvent.bind(document)
    };
    runSnippets(snippetsConfig);
  ` : "";

        const code = `(function () {
    const shimFn = (${shimFn});
    const shimConfigKey = "${shimConfigKey}";
    const runStorageShim = (${runStorageShim});
    runStorageShim(shimFn, shimConfigKey);
    ${snippetsCode}
  })();`;

        injectScript(code, policy);
    }
        ;

    const onFiltersReceived = event => {
        if (!event || !event.detail) {
            return;
        }

        const { type, filters, debug } = event.detail;

        // ignore other events that are not related to filters config
        if (type !== "ewe:filters-config") {
            return;
        }

        // Check which snippets need to be executed in the main world.
        const mainSnippets = [];
        for (const filter of filters) {
            for (const [name, ...args] of filter) {
                if (main.has(name)) {
                    mainSnippets.push([name, ...args]);
                }
            }
        }

        // sendDetectionEvent is intentionally not included in the main world env.
        // Detection events rely on ServerLogger and Sentry, which require extension
        // API access only available in the isolated world. See snippet-events.js.
        const snippetsConfig = {
            callback: main,
            env: {
                debugCSSProperties: debug ? DEBUG_CSS_PROPERTIES : null,
                sendSnippetHitEvent: createMainWorldHitEventSender(commsChannelName, isMainWorld ? nativeDispatch : null)
            },
            filters: mainSnippets,
            shimFn: shimStorage,
            shimConfigKey: CACHED_FILTERS_CONFIG_KEY,
            commsChannel: commsChannelName,
            serializeError: toSerializableError,
            dispatchFn: nativeDispatch
        };

        // If this script is injected into the main world we can execute directly.
        // If we are on isolated world (MV2), we need to create an inline script to
        // inject the snippets into page context.
        if (isMainWorld) {
            runStorageShim(shimStorage, CACHED_FILTERS_CONFIG_KEY);
            runSnippets(snippetsConfig);
        } else {
            appendSnippets(snippetsConfig);
        }
    }
        ;

    document.addEventListener(commsChannelName, onFiltersReceived);
    document.dispatchEvent(new CustomEvent(HANDSHAKE_EVENT_NAME));

    /******/
}
)();
