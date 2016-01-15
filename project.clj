(defproject clj-asterisk "0.2.5"
  :description "Clojure bindings for Asterisk Manager API"
  :url "http://www.github.com/guilespi/clj-asterisk"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.incubator "0.1.3"]
                 [midje "1.8.3"]
                 [slingshot "0.12.2"]
                 [org.clojure/tools.logging "0.3.1"]]
  :plugins [[lein-midje "3.1.3"]])
