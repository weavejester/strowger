(defproject strowger "0.1.4"
  :description "A ClojureScript library for managing DOM events"
  :url "https://github.com/weavejester/strowger"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145" :scope "provided"]]
  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-codox "0.9.4"]]
  :codox
  {:language    :clojurescript
   :metadata    {:doc/format :markdown}
   :output-path "codox"
   :source-uri  "http://github.com/weavejester/strowger/blob/{version}/{filepath}#L{line}"}
  :cljsbuild
  {:builds {:main {:source-paths ["src"]
                   :compiler {:output-to "target/main.js"
                              :optimizations :whitespace}}}})
