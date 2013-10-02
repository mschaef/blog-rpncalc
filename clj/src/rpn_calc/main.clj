(ns rpn-calc.main)

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

      'sto (fn [ { [rnum val & more] :stack regs :regs} ]
           { :stack more :regs (assoc regs rnum val)})

      'rcl (fn [ { [rnum & more] :stack regs :regs} ]
           { :stack (cons (regs rnum) more) })
      
      'drop (fn [ { [x & more] :stack } ]
              { :stack more})

      'quit (fn [ {  } ]
              false)
      })

(defn show-state [ { stack :stack } ]
  (doseq [[index val] (map list (range (count stack) 0 -1) (reverse stack) )]
    (printf "%d> %s\n" index val)))

(defn find-command [ object ]
  (if (number? object)
    (fn [ { stack :stack } ] { :stack (cons object stack) })
    (commands object)))

(defn apply-command [ command state ]
  (if-let [ state-update (command state)]
    (conj state state-update)
    false))

(defn read-command []
  (read-string (.trim (.readLine *in*))))

(defn -main
  "Main Entry point"
  []
  (loop [ state { :stack () :regs (vec (take 20 (repeat 0))) } ]
    (show-state state)
    (print "> ")
    (flush)
    (let [command (find-command (read-command))]
      (if-let [new-state (apply-command command state)]
        (recur new-state)
        nil)))
  (println "end run."))

