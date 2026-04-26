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

(deftest envelope
  (testing "invoke return results in an envelope."
    (let [arg {:hi :there}]
      (is (= (str arg) (:val (ext/invoke {:tool-alias :deps
                                          :preserve-envelope true
                                          :fn 'clojure.core/identity
                                          :args arg})))))))

(deftest invoke-alias
  (testing "invoke invoke with -x."
    (let [arg {:hi :there}]
      (is (= arg (ext/invoke {:alias :cli
                              :fn 'fmjrey.invoke/invoke
                              :args {:tool-alias :deps
                                     :fn 'clojure.core/identity
                                     :args arg}}))))))

(deftest invoke-dir
  (testing "invoke test-project -x."
    (let [arg {:test1 :test2}]
      (is (= arg (ext/invoke {:alias :cli
                              :dir "test-project"
                              :fn 'return
                              :args arg}))))))
