;; Copyright (c) KSM Technology Partners. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (https://opensource.org/licenses/EPL-2.0)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;;
;; You must not remove this notice, or any other, from this software.

(ns rpn-calc.macro)

(defmacro stack-op [ before after ]
  `(fn [ { [ ~@before & more# ] :stack } ]
     { :stack (concat ~after more# ) } ) )

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
