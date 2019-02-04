(ns luna.handlers
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [luna.utils :as utils]
            [medley.core :as medley]))

(def <sub (comp deref rf/subscribe))
(def >evt rf/dispatch)

;; ------------ S U B S C R I P T I O N S ------------ ;;

(rf/reg-sub
 (utils/luna :db)
 (fn [db _]
   db))

(rf/reg-sub
 (utils/luna :unprocessed-inputs)
 (fn [db [_ form-id]]
   (get-in db (utils/path->inputs form-id))))

(rf/reg-sub
 (utils/luna :input-value)
 (fn [db [_ form-id input-id]]
   (get-in db (utils/path->input form-id input-id))))

(rf/reg-sub
 (utils/luna :dropdown-value)
 (fn [db [_ form-id input-id]]
   (get-in db (utils/path->input form-id input-id))))

(rf/reg-sub
 (utils/luna :search-value)
 (fn [db [_ form-id input-id]]
   (get-in db (utils/path->input form-id input-id))))

(rf/reg-sub
 (utils/luna :submitting?)
 (fn [db [_ form-id]]
   (get-in db (utils/path->submit form-id :submitting?))))

(rf/reg-sub
 (utils/luna :first-submit-dispatched?)
 (fn [db [_ form-id]]
   (get-in db (utils/path->submit form-id :first-submit-dispatched?))))

(rf/reg-sub
 (utils/luna :errors?)
 (fn [[evt form-id]]
   [(rf/subscribe [(utils/luna :unprocessed-inputs) form-id])])
 (fn [[inputs]]
   (->> inputs
        (remove (fn [[_ {:keys [errors]}]] (empty? errors))))))

(rf/reg-sub
 (utils/luna :input-errors)
 (fn [db [_ form-id input-id]]
   (get-in db (utils/path->error form-id input-id))))

(rf/reg-sub
 (utils/luna :server-message)
 (fn [db [_ form-id]]
   (get-in db (utils/path->utils form-id :server-message))))

(rf/reg-sub
 (utils/luna :notification?)
 (fn [db [_ form-id]]
   (get-in db (utils/path->utils form-id :notification?))))

;; ------------ G E T  F O R M  V A L U E S ------------ ;;

;;; get form values in event from pure fn

(rf/reg-event-fx
 (utils/luna :do-something-with-form)
 (fn [{:keys [db]} [_ form-id]]
   (print (utils/collect-form-inputs
           db
           form-id))))

;; get form values from subscription

(rf/reg-sub
 (utils/luna :form-inputs)
 (fn [[evt form-id]]
   [(rf/subscribe [(utils/luna :unprocessed-inputs) form-id])])
 (fn [[inputs]]
   (utils/collect-form-inputs-recursion inputs)))

;; ------------ E V E N T S ------------ ;;

(rf/reg-event-db
 (utils/luna :clear-form)
 (fn [db [_ form-id]]
   (medley/dissoc-in db (utils/path->form-id form-id))))

(rf/reg-event-db
 (utils/luna :set-input-value)
 (fn [db [_ form-id input-id input-value]]
   (if input-value
     (assoc-in db (utils/path->input form-id input-id) input-value)
     (medley/dissoc-in db (utils/path->input form-id input-id)))))

(rf/reg-event-db
 (utils/luna :add-input-error)
 (fn [db [_ form-id input-id error]]
   (update-in db (utils/path->error form-id input-id) (fnil conj #{}) error)))

(rf/reg-event-db
 (utils/luna :remove-input-error)
 (fn [db [_ form-id input-id error]]
   (update-in db (utils/path->error form-id input-id) disj error)))

(rf/reg-event-db
 (utils/luna :set-submitting?)
 (fn [db [_ form-id value]]
   (assoc-in db (utils/path->submit form-id :submitting?) value)))

(rf/reg-event-db
 (utils/luna :set-first-submit-dispatched?)
 (fn [db [_ form-id value]]
   (assoc-in db (utils/path->submit form-id :first-submit-dispatched?) value)))

;; data shape accepted:
;; {:name "foo" :email "bar" ...}

(rf/reg-event-db
 (utils/luna :populate-form)
 (fn [db [_ form-id form-data]]
   (reduce
    (fn [new-state [input-id input-value]]
      (assoc-in new-state
                (utils/path->input form-id input-id) input-value))
    db form-data)))

(rf/reg-event-db
 (utils/luna :add-server-message)
 (fn [db [_ form-id message]]
   (assoc-in db (utils/path->utils form-id :server-message) message)))

(rf/reg-event-db
 (utils/luna :remove-server-message)
 (fn [db [_ form-id]]
   (medley/dissoc-in db (utils/path->utils form-id :server-message))))

(rf/reg-event-db
 (utils/luna :show-notification)
 (fn [db [_ form-id]]
   (assoc-in db (utils/path->utils form-id :notification?) true)))

(rf/reg-event-db
 (utils/luna :hide-notification)
 (fn [db [_ form-id]]
   (medley/dissoc-in db (utils/path->utils form-id :notification?))))

;; ------------ E V E N T S / S U B .  S T Y L E ------------ ;;

(rf/reg-event-db
 (utils/luna :store-icons)
 (fn [db [_ form-id input-id icons]]
   (assoc-in db (utils/path->utils form-id [input-id :icons]) icons)))

(rf/reg-sub
 (utils/luna :icons)
 (fn [db [_ form-id]]
   (get-in db (utils/path->utils form-id :icons))))

(rf/reg-sub
 (utils/luna :classes-input)
 (fn [[evt form-id input-id]]
   [(rf/subscribe [(utils/luna :first-submit-dispatched?) form-id])
    (rf/subscribe [(utils/luna :submitting?) form-id])
    (rf/subscribe [(utils/luna :input-errors) form-id input-id])
    (rf/subscribe [(utils/luna :icons) form-id])])
 (fn [[show? disabled? errors icons] [form-id input-id]]
   (let [style #(if (empty? errors) %1 %2)]
     (cond
       disabled?
       {:disabled? "disabled"}
       show?
       (merge
        {:input (style "is-success" "is-danger")
         :p "is-danger"
         :error (first errors)}
        (when icons
          {:control ""
           :icon-right (str "fas "(style (:icon-right-success icons)
                                         (:icon-right-danger icons)))}))
       :else {}))))

(rf/reg-sub
 (utils/luna :utils-submit)
 (fn [[evt form-id]]
   [(rf/subscribe [(utils/luna :first-submit-dispatched?) form-id])
    (rf/subscribe [(utils/luna :submitting?) form-id])
    (rf/subscribe [(utils/luna :errors?) form-id])])
 (fn [[first-submit-dispatched? submitting? errors] _]
   (let [button-classes
         (cond->> (str)
           submitting? (str "is-loading "))]
     {:first-dispatched? first-submit-dispatched?
      :button-classes button-classes
      :disabled (when (and (seq errors) first-submit-dispatched?) true)
      :submitting? submitting?
      :errors errors})))

;; ------------ F R O N T E N D  T E S T ------------ ;;

(rf/reg-event-fx
 (utils/luna :simulate-api-server-response)
 (fn [{:keys [db]} [_ form-id]]
   (print (utils/collect-form-inputs db form-id))
   {:dispatch-n [[(utils/luna :set-submitting?) form-id false]
                 [(utils/luna :set-first-submit-dispatched?) form-id false]]}))

(rf/reg-event-fx
 (utils/luna :test-submit)
 (fn [{:keys [db]} [_ form-id]]
   {:dispatch-later [{:ms 1000
                      :dispatch [(utils/luna :simulate-api-server-response) form-id]}]}))
