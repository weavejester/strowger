(defproject strowger/example "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [strowger "0.1.2"]]
  :plugins [[lein-cljsbuild "1.1.0"]]
  :cljsbuild
  {:builds {:main {:source-paths ["src"]
                   :compiler {:output-to "target/main.js"
                              :main example.core}}}})
