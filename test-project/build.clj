(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]
            [clojure.java.io :as io]))

(def lib 'fmjrey/invoke-test)
(def version "0.1.0-SNAPSHOT")
(def target-dir "target")
(def class-dir (str (io/file target-dir "classes")))

(defn- pom-template [version]
  [[:description "FIXME: my new library."]
   [:url "https://github.com/fmjrey/invoke"]
   [:licenses
    [:license
     [:name "Eclipse Public License 1.0"]
     [:url "http://www.eclipse.org/legal/epl-v10.html"]]]
   [:developers
    [:developer
     [:name "Francois"]]]
   [:scm
    [:url "https://github.com/fmjrey/invoke"]
    [:connection "scm:git:https://github.com/fmjrey/invoke.git"]
    [:developerConnection "scm:git:ssh:git@github.com:fmjrey/invoke.git"]
    [:tag (str "v" version)]]])

(defn- uber-opts [opts]
  (assoc opts
         :lib lib    :version version
         :uber-file  (str (io/file target-dir "invoke-test.jar"))
         :basis      (b/create-basis {})
         :class-dir  class-dir
         :src-dirs   ["src"]
         ;:main       test.project
         ;:ns-compile [test.project]
         ))

(defn clean [_]
  (b/delete {:path target-dir})
  (b/delete {:path ".cpcache"}))


(defn uberjar [opts]
  (clean opts)
  (let [opts (uber-opts opts)]
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs ["resources" "src"] :target-dir class-dir})
    (println (str "\nCompiling ..."))
    (b/compile-clj opts)
    (println "\nBuilding JAR..." (:uber-file opts))
    (b/uber opts))
  opts)
