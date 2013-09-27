(ns rpn-calc.main)

(defn error [ message ]
  (println "Error: " message))

(defn push [ x stack ]
  (cons x stack))

(defn add [ stack ]
  (let [[x y & more] stack]
    (cons (+ y x) more)))

(defn sub [ stack ]
  (let [[x y & more] stack]
    (cons (- y x) more)))

(defn mul [ stack ]
  (let [[x y & more] stack]
    (cons (* y x) more)))

(defn div [ stack ]
  (let [[x y & more] stack]
    (cons (/ y x) more)))

(defn swap [ stack ]
  (let [[x y & more] stack]
    (cons y (cons x more))))

(defn dup [ stack ]
  (let [[x & more] stack]
    (cons x (cons x more))))

(defn drop1 [ stack ]
  (let [[x & more] stack]
    more))

(defn quit [ stack ]
  false)

(defn show-stack [ stack ]
  (doseq [[index val] (map list (range (count stack) 0 -1) (reverse stack) )]
    (printf "%d> %s\n" index val)))

(defn prompt []
  (print "> ")
  (flush))

(defn read-token [ ]
  (println "read-token")
  (clojure.main/skip-whitespace *in*)
  (println (str "skipped-whitespace: "))
  (read *in* false nil))

(defn rpn-eval [ object stack ]
  (cond (number? object) (push object stack)
        (symbol? object) ((eval object) stack)
        :else (error "Bad value")))

(defn rpn-repl []
  (let [request-exit (Object.)]
    (loop [ stack () ]
      (show-stack stack)
      (prompt)
      (if-let [new-stack (rpn-eval (read-token) stack)]
        (recur new-stack)
        nil))))

(defn -main
  "Main Entry point"
  []
  (rpn-repl)
  (println "end run."))

