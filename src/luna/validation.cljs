(ns luna.validation
  (:require
   [re-frame.core :as rf]
   [luna.handlers :refer [<sub >evt]]))

;; ------------ Dispatch one or multiple validations ------------ ;;

(defmulti validate
  (fn [data-structure form-id input-id]
    (type data-structure)))

(defmethod validate
  (type [])
  [validate-many form-id input-id]
  (doseq [{:keys [valid? error]} validate-many]
    (if (valid? (<sub [:luna/input-value form-id input-id]))
    (>evt [:luna/remove-input-error form-id input-id error])
    (>evt [:luna/add-input-error form-id input-id error]))))

(defmethod validate
  (type {})
  [{:keys [valid? error]} form-id input-id]
  (if (valid? (<sub [:luna/input-value form-id input-id]))
    (>evt [:luna/remove-input-error form-id input-id error])
    (>evt [:luna/add-input-error form-id input-id error])))

(defmethod validate
  nil
  [])

;; ------------ Dispatch one or multiple validations ------------ ;;
