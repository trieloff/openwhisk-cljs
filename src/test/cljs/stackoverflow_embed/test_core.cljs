(ns stackoverflow-embed.test-core
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test-numbers
  (is (= 1 1)))

(enable-console-print!)
(cljs.test/run-tests)