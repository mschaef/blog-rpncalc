(ns rpn-calc.dfcompile)

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

(defn apply-stack-op [ initial-state stack-op ]
  (let [ { initial-stack :stack initial-bindings :bindings } initial-state
         { before-pic :before-pic after-pic :after-pic } (meta stack-op)
         { remaining :stack bindings :bindings} (formal-bindings initial-state before-pic)
         after-pic-bindings (map (fn [ post-stack-element ] 
                                   [(gensym) (apply-substitutions post-stack-element bindings)])
                                 after-pic)]
    {
     :stack (concat (map first after-pic-bindings) remaining)
     :bindings (reduce (fn [ bindings [ sym binding ] ]
                         (assoc bindings sym binding))
                       initial-bindings
                       after-pic-bindings)}))

(defn dummy-stack []
  (map #(symbol (str "stack-" %)) (range)))
 
(defn compile-composite-command [ cmd-names ]
  (reduce apply-stack-op
          { :bindings {} :stack (dummy-stack)}
          (map find-command cmd-names)))

(defn make-push-command [ object ] 
  (stack-op [ ] [ object ]))

(defn show-state [ { stack :stack } ]
  (doseq [[index val] (map list (range (count stack) 0 -1) (reverse stack) )]
    (printf "%d> %s\n" index val)))

(defn apply-command [ initial-state command  ]
  (if-let [ state-update (command initial-state)]
    (assoc (conj initial-state state-update) :prev initial-state)
    false))

(defn find-command [ object ]
  (if (number? object)
    (make-push-command object)
    (commands object)))

(defn parse-single-command [ str ]
  (find-command (read-string str)))

(defn make-composite-command [ subcommands ]
  (fn [ state ]
    (reduce apply-command state subcommands)))

(defn parse-command-string [ str ]
  (make-composite-command
   (map parse-single-command (.split (.trim str) "\\s+"))))

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