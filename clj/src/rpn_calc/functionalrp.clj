(ns rpn-calc.functionalrp)

(def commands
     {
      '+ (fn [ { [x y & more] :stack } ]
           { :stack (cons (+ y x) more)})
      
      '- (fn [ { [x y & more] :stack } ]
           { :stack (cons (- y x) more)})

      '* (fn [ { [x y & more] :stack } ]
           { :stack (cons (* y x) more)})

      '/ (fn [ { [x y & more] :stack } ]
           { :stack (cons (/ y x) more)})

      'sqrt (fn [ { [x & more] :stack } ]
           { :stack (cons (Math/pow x 0.5) more)})

      'sto (fn [ { [rnum val & more] :stack regs :regs} ]
           { :stack more :regs (assoc regs rnum val)})

      'rcl (fn [ { [rnum & more] :stack regs :regs} ]
           { :stack (cons (regs rnum) more) })
      
      'drop (fn [ { [x & more] :stack } ]
              { :stack more })

      'undo (fn [ { prev :prev } ]
              prev )

      'quit (fn [ {  } ]
              false)
      })

(defn make-push-command [ object ] 
  (fn [ { stack :stack } ]
    { :stack (cons object stack) }))

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
  {
   :stack ()
   :regs (vec (take 20 (repeat 0)))
   })

(defn main []
  (loop [ state (make-initial-state) ]
    (let [command (parse-command-string (read-command-string state))]
      (if-let [new-state (apply-command state command)]
        (recur new-state)
        nil))))