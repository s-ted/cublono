(defproject cubane/cublono "0.1.0-SNAPSHOT"
  :description      "Lisp style templating"
  :url              "http://github.com/s-ted/cublono"
  :author           "Sylvain Tedoldi"
  :min-lein-version "2.0.0"
  :license          {:name "Eclipse Public License"
                     :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies     [[org.clojure/clojure "1.8.0" :scope "provided"]
                     [org.clojure/clojurescript "1.7.228" :scope "provided"]]
  :aliases          {"ci" ["do"
                           ["clean"]
                           ["test" ":default"]
                           ["doo" "phantom" "none" "once"]
                           ["doo" "phantom" "advanced" "once"]]}
  :clean-targets    ^{:protect false} [:target-path]
  :doo              {:build "test"}
  :cljsbuild        {:builds
                     {:test
                      {:compiler
                       {:asset-path "target/public/test"
                        :main cublono.test-runner
                        :output-to "target/public/cublono.js"
                        :output-dir "target/public/test"
                        :optimizations :none
                        :pretty-print true
                        :source-map true
                        :verbose true}
                       :source-paths ["src" "test"]}

                      :none
                      {:compiler
                       {:asset-path "target/none/out"
                        :main cublono.test-runner
                        :output-to "target/none/cublono.js"
                        :output-dir "target/none/out"
                        :optimizations :none
                        :pretty-print true
                        :source-map true
                        :verbose true}
                       :source-paths ["src" "test"]}

                      :advanced
                      {:compiler
                       {:asset-path "target/advanced/out"
                        :main cublono.test-runner
                        :output-to "target/advanced/cublono.js"
                        :optimizations :advanced
                        :pretty-print true
                        :verbose true}
                       :source-paths ["src" "test"]}}}
  :profiles           {:dev {:plugins [[lein-cljsbuild "1.1.2"]
                                       [lein-doo "0.1.6"]]}})
