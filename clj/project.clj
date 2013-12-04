(defproject rpn-calc "0.1.0-SNAPSHOT"
  :description "KSM Partners - RPN Calculator"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.0"]]

  ;:plugins [[lein-nodisassemble "0.1.2"]]  

  :repl-options {
                 :host "0.0.0.0"
                 :port 53095
                 }

  :main rpn-calc.main)