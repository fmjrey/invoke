(ns fmjrey.invoke-test
  (:require [clojure.test :refer [deftest is testing]]
            [fmjrey.invoke :as ext]))

(deftest invoke-result
  (testing "invoke return expected results."
    (let [arg {:hi :there}]
      (is (= arg (ext/invoke {:tool-alias :deps
                              :fn 'clojure.core/identity
                              :args arg}))))))

(deftest invoke-throws
  (testing "invoke rethrows."
    (is (thrown? clojure.lang.ExceptionInfo
                 (ext/invoke {:tool-alias :deps
                              :fn 'clojure.core/+
                              :args {:fail :here}})))))

(deftest preserve-envelope
  (testing "invoke return results in an envelope."
    (let [arg {:hi :there}]
      (is (= (str arg) (:val (ext/invoke {:tool-alias :deps
                                          :preserve-envelope true
                                          :fn 'clojure.core/identity
                                          :args arg})))))))

(deftest invoke-alias
  (testing "invoke invoke with -X:cli."
    (let [arg {:hi :there}]
      (is (= arg (ext/invoke {:alias :cli
                              :fn 'fmjrey.invoke/invoke
                              :args {:tool-alias :deps
                                     :fn 'clojure.core/identity
                                     :args arg}}))))))

(deftest invoke-dir
  (testing "invoke test-project with -X:cli."
    (let [arg {:test1 :test2}]
      (is (= arg (ext/invoke {:alias :cli
                              :dir "test-project"
                              :fn 'return
                              :args arg}))))))

(deftest invoke-build
  (testing "invoke test-project with -T:build uberjar."
    (let [arg {}]
      (is (= arg (ext/invoke {:tool-alias :build
                              :dir "test-project"
                              :fn 'uberjar
                              :args arg}))))))

(deftest invoke-cp
  (testing "invoke test-project with -X and -Scp."
    (let [arg {:test1 :test2}]
      (is (= arg (ext/invoke {:cp "test-project/target/invoke-test.jar"
                              :fn 'test.project/return
                              :args arg})))))
  (testing "invoke test-project with dir -X and -Scp."
    (let [arg {:test1 :test2}]
      (is (= arg (ext/invoke {:cp "target/invoke-test.jar"
                              :dir "test-project"
                              :fn 'test.project/return
                              :args arg})))))
  (testing "invoke test-project with dir -X:cli and -Scp."
    (let [arg {:test1 :test2}]
      (is (= arg (ext/invoke {:cp "target/invoke-test.jar"
                              :dir "test-project"
                              :alias :cli
                              :fn 'return
                              :args arg})))))
  (testing "invoke test-project with -T and -Scp."
    (let [arg {:test1 :test2}]
      (is (= arg (ext/invoke {:cp "test-project/target/invoke-test.jar"
                              :tool-name ""
                              :fn 'test.project/return
                              :args arg}))))))

(defn test-ns-hook []
  (invoke-result)
  (invoke-throws)
  (preserve-envelope)
  (invoke-alias)
  (invoke-dir)
  (invoke-build)
  (invoke-cp))
