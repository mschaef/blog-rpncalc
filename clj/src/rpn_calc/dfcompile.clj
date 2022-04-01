;; Copyright (c) KSM Technology Partners. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (https://opensource.org/licenses/EPL-2.0)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;;
;; You must not remove this notice, or any other, from this software.

(ns rpn-calc.dfcompile
  (:require [clojure.set :refer [difference]]))

(defmacro stack-op [ before-pic after-pic ]
  `(with-meta (fn [ { [ ~@before-pic & more# ] :stack } ]
                { :stack (concat ~after-pic more# ) } )
     { :before-pic '~before-pic :after-pic '~after-pic}))

(def commands
     {
      '+ (stack-op [x y] [(+ x y)])
      '- (stack-op [x y] [(- y x)])
      '* (stack-op [x y] [(* x y)])
      '/ (stack-op [x y] [(/ y x)])
      'sqrt (stack-op [x] [(Math/pow x 0.5)])
      'drop (stack-op [ x ] [ ])
      'dup (stack-op [ x ] [ x x ])
      'swap (stack-op [ x y ] [ y x ])

      'sto (fn [ { [rnum val & more] :stack regs :regs} ]
           { :stack more :regs (assoc regs rnum val)})

      'rcl (fn [ { [rnum & more] :stack regs :regs} ]
           { :stack (cons (regs rnum) more) })

      'undo (fn [ { prev :prev } ]
              prev )

      'quit (fn [ {  } ]
              false)
      })

(defn make-push-command [ object ]
  (eval `(stack-op [ ] [ ~object ])))

(defn find-command [ object ]
  (if (number? object)
    (make-push-command object)
    (commands object)))

(defn temp-sym? [ val ]
  (and (symbol? val)
       (.startsWith (name val) "temp-")))

(defn ahash-set [ seqs ] (apply hash-set seqs))

(defn all-symbols [ form ]
  (cond (symbol? form) (hash-set form)
        (seq? form) (ahash-set (filter symbol? (mapcat all-symbols form)))
        (or (map? form) (set? form)) (all-symbols (flatten (seq form)))
        :else #{}))

(defn apply-substitutions [ form bmap ]
  (if (seq? form)
    (map #(apply-substitutions % bmap) form)
    (or (bmap form)
        form)))

(defn gen-temp-sym []
  (gensym "temp-"))

(defn apply-stack-op-to-state [ initial-state stack-op ]
  (let [ before-bmap (reduce (fn [ bmap [ stack-elem formal ] ]
                               (assoc bmap formal stack-elem))
                             (:bmap initial-state)
                             (map list (:stack initial-state) (:before-pic (meta stack-op))))

        after-bpairs
        (map (fn [ after-pic-elem ]
               (let [ expanded (apply-substitutions after-pic-elem before-bmap)]
                 [(if (symbol? expanded) expanded (gen-temp-sym))
                  expanded]))
             (:after-pic (meta stack-op)))]
    {
     :stack (concat (map first after-bpairs)
                    (drop (count (:before-pic (meta stack-op))) (:stack initial-state)))

     :bmap (reduce (fn [ after-bmap [ sym binding ]]
                     (if (= sym binding)
                       after-bmap
                       (assoc after-bmap sym binding)))
                   (:bmap initial-state)
                   after-bpairs)}))

(defn dummy-stack []
  (map #(symbol (str "stack-" %)) (range)))

(defn initial-state []
  { :bmap {} :stack (dummy-stack)})

(defn composite-command-effect [ cmd-names ]
  (reduce apply-stack-op-to-state
          (initial-state)
          (map find-command cmd-names)))

(defn normalize-dep-map [ dep-map ]
  (reduce (fn [ dep-map sym ]
            (if (contains? dep-map sym)
              dep-map
              (assoc dep-map sym #{})))
          dep-map
          (all-symbols dep-map)))

(defn bmap-dep-map [ bmap ]
  (normalize-dep-map
   (reduce (fn [ dep-map [ sym form ]]
             (assoc dep-map sym (all-symbols form)))
           {}
           bmap)))

(defn keys-satisfying [ kvs pred? ]
  (reduce (fn [ good-keys key ]
            (if (pred? (kvs key))
              (conj good-keys key)
              good-keys))
          #{}
          (keys kvs)))

(defn non-empty-set? [ set ]
  (and (> (count set) 0)
       set))

(defn dep-map-ordering [ dep-map ]
  (loop [ tiers [] dep-map dep-map ]
    (if (empty? dep-map)
      tiers
      (if-let [ satisfied (non-empty-set? (keys-satisfying dep-map empty?))]
        (recur (conj tiers satisfied )
               (reduce (fn [ dep-map [ sym deps ]]
                         (if (contains? satisfied sym)
                           dep-map
                           (assoc dep-map sym (difference deps satisfied))))
                       {}
                       dep-map))
        :circular-deps))))

(defn bmap-stack-symbols [ bmap ]
  (take-while (ahash-set (mapcat all-symbols (vals bmap)))
              (dummy-stack)))

(defn bmap-binding-order [ bmap ]
  (filter temp-sym? (mapcat seq (dep-map-ordering (bmap-dep-map bmap)))))

(defn composite-command-form [ cmd-names ]
  (let [ { bmap :bmap stack :stack } (composite-command-effect cmd-names)
         before-pic (bmap-stack-symbols bmap)
         after-pic (take-while temp-sym? stack)]

    `(fn [ { [ ~@before-pic & more# ] :stack } ]
       (let ~(vec (mapcat (fn [ sym ] `(~sym ~(bmap sym)))
                          (bmap-binding-order bmap)))
         {:stack (concat ~(vec after-pic) more#)}))))

(defn compile-composite-command [ cmd-names ]
  (eval (composite-command-form cmd-names)))

(defn show-state [ { stack :stack } ]
  (doseq [[index val] (map list (range (count stack) 0 -1) (reverse stack) )]
    (printf "%d> %s\n" index val)))

(defn apply-command [ initial-state command  ]
  (if-let [ state-update  (command initial-state) ]
    (assoc (conj initial-state state-update) :prev initial-state)
    false))

(defn parse-single-command [ cmd-name ]
  (cond (string? cmd-name) (find-command (read-string cmd-name))
        (symbol? cmd-name) (find-command cmd-name)
        :else :no-command))

(defn make-composite-command [ cmd-names ]
  (fn [ state ]
    (reduce apply-command state (map parse-single-command cmd-names))))

(defn parse-command-string [ str ]
  (make-composite-command (.split (.trim str) "\\s+")))

(defn read-command-string [ state ]
  (show-state state)
  (print "> ")
  (flush)
  (.readLine *in*))

(defn make-initial-state []
  { :stack () :regs (vec (take 20 (repeat 0))) })

(defn main []
  (loop [ state (make-initial-state) ]
    (let [command (parse-command-string (read-command-string state))]
      (if-let [new-state (apply-command state command)]
        (recur new-state)
        nil))))

(def dist-3 '[dup * swap dup * + swap dup * + sqrt])

(defn bench []
  (let [ dist-3-cfc (compile-composite-command dist-3)
        dist-3-ifc (make-composite-command dist-3)
        n 500000]

    (println 'dist-3-ifc)
    (time (dotimes [ _ n ] (dist-3-ifc { :stack [ 1 2 3 ] })))
    (println 'dist-3-cfc)
    (time (dotimes [ _ n ] (dist-3-cfc { :stack [ 1 2 3 ] })))))
