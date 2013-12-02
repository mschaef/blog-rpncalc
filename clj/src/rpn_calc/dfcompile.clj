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
  (stack-op [ ] [ object ]))

(defn find-command [ object ]
  (if (number? object)
    (make-push-command object)
    (commands object)))

(defn formal-bindings [  { stack :stack initial-bindings :bindings } formals ]
  {
   :stack  (drop (count formals) stack)
   :bindings (reduce (fn [ bindings [ stack-elem formal ] ]
                       (assoc bindings formal stack-elem))
                     initial-bindings
                     (map list stack formals))})

(defn apply-substitutions [ form bindings ]
  (if (seq? form)
    (map #(apply-substitutions % bindings) form)
    (or (bindings form)
        form)))

(defn gen-temp-sym []
  (gensym "temp-"))

(defn symbol-starting-with? [ val prefix ]
  (and (symbol? val)
       (.startsWith (name val) prefix)))

(defn temp-sym? [ val ] (symbol-starting-with? val "temp-"))

(defn stack-sym? [ val ] (symbol-starting-with? val "stack-"))

(defn ahash-set [ seqs ] (apply hash-set seqs))

(defn referenced-symbols [ form ]
    (cond (symbol? form) (hash-set form)
          (seq? form) (ahash-set (mapcat referenced-symbols (rest form)))
          (map? form) (referenced-symbols (concat (keys form) (vals form)))
          (set? form) (filter symbol? form)
          :else ()))

(defn update-stack-state [ initial-state stack-op ]
  (let [ { initial-stack :stack initial-bindings :bindings }
         initial-state

         { before-pic :before-pic after-pic :after-pic }
         (meta stack-op)

         { remaining :stack bindings :bindings}
         (formal-bindings initial-state before-pic)

         after-pic-bindings
         (map (fn [ post-stack-element ] 
                [(gen-temp-sym)
                 (apply-substitutions post-stack-element bindings)])
              after-pic)]
    {
     :stack (concat (map first after-pic-bindings) remaining)
     :bindings (reduce (fn [ bindings [ sym binding ] ]
                         (assoc bindings sym binding))
                       initial-bindings
                       after-pic-bindings)}))

(defn dummy-stack []
  (map #(symbol (str "stack-" %)) (range)))

(defn composite-command-effect [ cmd-names ]
  (reduce update-stack-state
          { :bindings {} :stack (dummy-stack)}
          (map find-command cmd-names)))

(defn normalize-dep-map [ dep-map ]
  (reduce (fn [ dep-map sym ]
            (if (contains? dep-map sym)
              dep-map
              (assoc dep-map sym #{})))
          dep-map
          (referenced-symbols dep-map)))

(defn binding-dep-map [ bindings ]
  (normalize-dep-map
   (reduce (fn [ dep-map [ binding form ]]
             (assoc dep-map binding (referenced-symbols form)))
           {}
           bindings)))

(defn keys-satisfying [ kvs pred? ]
  (reduce (fn [ good-keys key ]
            (if (pred? (kvs key))
              (conj good-keys key)
              good-keys))
          #{}
          (keys kvs)))

(defn dep-map-ordering [ dep-map ]
  (reverse
   (loop [ tiers ()  dep-map dep-map ]
     (let [ computable-syms (keys-satisfying dep-map empty?)]
       (cond (empty? dep-map) tiers
             (empty? computable-syms) :circular-deps
             :else (recur (cons computable-syms tiers)
                          (reduce (fn [ dep-map [ binding deps ]]
                                    (if (contains? computable-syms binding)
                                      dep-map
                                      (assoc dep-map binding (difference deps computable-syms))))
                                  {}
                                  dep-map)))))))

(defn composite-command-form [ cmd-names ]
  (let [ { bindings :bindings stack :stack }
         (composite-command-effect cmd-names)

         before-pic
         (take-while (ahash-set (mapcat referenced-symbols (vals bindings)))
                     (dummy-stack))

         after-pic
         (take-while temp-sym? stack)

         binding-order
         (remove stack-sym?
                 (mapcat seq (dep-map-ordering (binding-dep-map bindings))))]
 
    `(fn [ { ~(vec before-pic) :stack } ]
       (let ~(vec (mapcat (fn [ binding ]
                            `(~binding ~(bindings binding)))
                          binding-order))
         {:stack ~(vec after-pic)}))))

(def dist-3 '[dup * swap dup * + swap dup * + sqrt])

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