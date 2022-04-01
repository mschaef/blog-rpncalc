;; Copyright (c) KSM Technology Partners. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (https://opensource.org/licenses/EPL-2.0)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;;
;; You must not remove this notice, or any other, from this software.

(ns rpn-calc.main
  (:require rpn-calc.functionalrp
            rpn-calc.macro
            rpn-calc.dfcompile))

(defn show-choices [ choices ]
  (doseq [[index choice] (map list (range) choices)]
    (printf "%d> %s\n" index choice)))

(defn prompted-read []
  (print "choose> ")
  (flush)
  (.readLine *in*))

(defn as-integer? [ str ]
  (try
   (and (string? str)
        (Integer/parseInt (.trim str)))
   (catch Exception ex
     false)))

(defn within [ x lo hi ]
  (and (>= x lo) (< x hi) x))

(defn choose [ choices ]
  (loop []
    (show-choices choices)
    (if-let [ number (as-integer? (prompted-read))]
      (or (within number 0 (count choices))
          (do
            (println "Out of range, retry.")
            (recur)))
      (do
        (println "Invalid number, quitting.")
        false))))

(defmacro choose-case [ & clauses ]
  (let [pc (partition 2 clauses)]
    `(case (choose '~(apply vector (map first pc)))
           ~@(apply concat ( map #(list %1 %2) (range) (map second pc))))))

(defn -main []
  (choose-case
   rpn-calc.functionalrp (rpn-calc.functionalrp/main)
   rpn-calc.macro (rpn-calc.macro/main)
   rpn-calc.dfcompile (rpn-calc.dfcompile/main)))
