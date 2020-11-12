(ns reajure.native
  (:require
   #?@(:cljs [["react" :as r]
              ["react-native" :as rn]
              [goog.object]
              [cljs-bean.core]
              [rewrap.dev.refresh]]
       :clj [[rewrap.hiccup :as hiccup]
             [rewrap.component :as comp]
             [rewrap.dev.refresh :as refresh]
             [reajure.native.stylesheet :as stylesheet]]))
  #?(:cljs (:require-macros [reajure.native])))

#?(:cljs
   (do (def createElement r/createElement)
       (def createStyleSheet (.-create rn/StyleSheet))))

#?(:clj
   (do
     (declare ^:dynamic *opts*)

     (defn- emit-element
       "Emit react element expr."
       [type props & children]
       `(reajure.native/createElement ~type ~props ~@children))

     (defn- compile-hiccup
       "Compile any hiccup in component `expr`.
        Accepts `parse-props` fn for parsing props before they're converted to js object."
       [expr parse-props]
       (let [parsers (conj
                      (:parsers *opts*)
                      [[any? (fn [t p ch]
                               [t
                                (-> p
                                    parse-props
                                    comp/->props)
                                ch])]])]
         (hiccup/compile expr {:emitter emit-element
                               :parsers parsers})))

     (defn- render-expr
       "Render hiccup `component-expr` to react/createElement expr. 
        Accepts optional `style-sym` for referencing compiled styles.
        If styles-sym is not passed, does not parse styles and returns element-expr.
        If styles-sym is passed, parses styles and returns tuple of [element-expr styles-expr]."
       ([component-expr] (render-expr component-expr nil))
       ([component-expr styles-sym]
        (let [styles     (atom {})]
          (letfn [(replace-style-shorthands
                    [v]
                    (let [style-key (str (gensym "style"))]
                      ;; # todo should we be passing all styles to compiler? or only keyword styles
                      (swap! styles assoc style-key (stylesheet/compile v {:rem 16}))
                      `(cljs.core/into-array
                        ~(conj
                          (filterv #(not (keyword? %)) v)
                          `(goog.object/get ~styles-sym ~style-key)))))
                  (parse-style
                    [x]
                    (if (and (vector? x) styles-sym)
                      (replace-style-shorthands x)
                      x))
                  (parse-props
                    [m]
                    (if (and (map? m) (seq (:style m)))
                      (assoc m :style (parse-style (:style m)))
                      m))]
            (let [element-expr (compile-hiccup component-expr parse-props)]
              (if styles-sym
                (let [styles       @styles
                      styles-expr  (when (seq styles)
                                     `(reajure.native/createStyleSheet (cljs.core/clj->js ~styles)))]
                  [element-expr styles-expr])
                element-expr))))))

     (defn fc*
       "Generate fn component with its respective style definitions"
       ([decls] (fc* decls {}))
       ([decls opts]
        (binding [*opts* opts]
          (let [{:keys [name docstr params body]} (comp/conform decls)
                comp-id (str *ns* "/" name)
                {:keys [def-refresh init-refresh hookup-refresh]} (refresh/exprs* comp-id name body)
                js-props?      (-> params first meta :tag (= 'js))
                wrap-props     (if-not js-props? (fn [p] `(cljs-bean.core/bean ~p)) identity)
                styles-sym     (gensym (str name "-styles"))
                eval-exprs     (cons hookup-refresh
                                     (butlast body))
                component-expr  (last body)
                [element-expr styles-expr] (render-expr component-expr styles-sym)]
            `(do
               ~def-refresh
               ~@(when styles-expr
                   [`(def ~styles-sym ~styles-expr)])
               (def ~@(if docstr [name docstr] [name])
                 ~(comp/generate [name docstr params eval-exprs element-expr]
                                 {:wrap-props-param wrap-props}))

               ~init-refresh)))))

     (defmacro defc
       "Define fn component."
       [& decls]
       (fc* decls))))
