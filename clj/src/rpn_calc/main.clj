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

(defn rpn-eval [ object stack ]
  (binding [ *ns* (find-ns 'rpn-calc.main)]
    (cond (number? object) (push object stack)
          (symbol? object) ((eval object) stack)
          :else (error "Bad value"))))

(defn read-command []
  (read-string (.trim (.readLine *in*))))

(defn rpn-repl []
  (loop [ stack () ]
    (show-stack stack)
    (prompt)
    (let [cmd (read-command)]
      (if-let [new-stack (rpn-eval cmd stack)]
        (recur new-stack)
        nil))))

(defn -main
  "Main Entry point"
  []
  (rpn-repl)
  (println "end run."))

