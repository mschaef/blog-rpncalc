(ns rpn-calc.main)

(defn error [ message ]
  (println "Error: " message))

(def commands
     {
      '+ (fn [ stack ]
             (let [[x y & more] stack]
               (cons (+ y x) more)))

      '- (fn [ stack ]
             (let [[x y & more] stack]
               (cons (- y x) more)))

      '* (fn [ stack ]
             (let [[x y & more] stack]
               (cons (* y x) more)))

      '/ (fn [ stack ]
             (let [[x y & more] stack]
               (cons (/ y x) more)))

      'swap (fn [ stack ]
              (let [[x y & more] stack]
                (cons y (cons x more))))

      'dup (fn [ stack ]
             (let [[x & more] stack]
               (cons x (cons x more))))

      'drop (fn [ stack ]
              (let [[x & more] stack]
                more))

      'quit (fn [ stack ]
              false)
      })

(defn show-stack [ stack ]
  (doseq [[index val] (map list (range (count stack) 0 -1) (reverse stack) )]
    (printf "%d> %s\n" index val)))

(defn find-command [ object ]
  (cond (number? object) (fn [ stack ] (cons object stack))
        (symbol? object) (commands object)
        :else (error "Bad value")))

(defn read-command []
  (read-string (.trim (.readLine *in*))))

(defn -main
  "Main Entry point"
  []
  (loop [ stack () ]
    (show-stack stack)
    (print "> ")
    (flush)
    (let [cmd (read-command)]
      (if-let [new-stack ((find-command cmd) stack)]
        (recur new-stack)
        nil)))
  (println "end run."))

