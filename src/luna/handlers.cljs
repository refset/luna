(ns luna.handlers
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [medley.core :as medley]))

(def <sub (comp deref rf/subscribe))
(def >evt rf/dispatch)

;; ------------ N A M E S P A C E ------------ ;;

(defn- luna
  [handler]
  (keyword (str "luna/" (name handler))))

(defn- to->ns
  [form-id handler]
  (when form-id
    (keyword (str (name form-id) "/" (name handler)))))

;; ------------ P A T H S ------------ ;;

(defn- flatten->path
  [& path-seq]
  (into [] (flatten path-seq)))

(defn- path->form-id
  [form-id]
  (flatten->path
   (luna :forms)
   form-id
   (to->ns form-id :inputs)))

(defn- path->input
  [form-id input-id]
  (flatten->path
   (luna :forms)
   form-id
   (to->ns form-id :inputs)
   input-id :value))

(defn- path->error
  [form-id input-id]
  (flatten->path
   (luna :forms)
   form-id
   (to->ns form-id :inputs)
   input-id :errors))

(defn- path->submit
  [form-id flag]
  (flatten->path
   (luna :forms)
   form-id
   (to->ns form-id :submit)
   flag))

(defn- path->utils
  [form-id flag]
  (flatten->path
   (luna :forms)
   form-id
   (to->ns form-id :utils)
   flag))

;; ------------ H E L P E R S ------------ ;;

;; example on how to get a subscription in event-db
;; from reg-event-db call -> (get-input-error..)
(defn- dummy
  [db form-id]
  (get-in db (path->form-id form-id)))

(rf/reg-sub
 (luna :dummy)
 (fn [db [_ form-id]]
   (reagent.ratom/reaction (dummy @db form-id))))
(defn- filter-nodes
  [f node]
  (into {} (filter (comp f val) node)))

(defn collect-form-inputs-recursion
  [node]
  (cond
    (contains? node :value)
    (:value node)

    :else
    (filter-nodes
     #(and (some? %)
           (if  (seqable? %)
             (not (empty? %))
             true))
     (zipmap
      (keys node)
      (map collect-form-inputs-recursion
           (when (not (set? node)) (vals node)))))))

(defn collect-form-inputs
  [form-inputs form-id]
  (collect-form-inputs-recursion form-inputs))

;; ------------ S U B S C R I P T I O N S ------------ ;;

(rf/reg-sub
 (luna :db)
 (fn [db _]
   db))

(rf/reg-sub
 (luna :unprocessed-inputs)
 (fn [db [_ form-id]]
   (get-in db (path->form-id form-id))))

(rf/reg-sub
 (luna :input-value)
 (fn [db [_ form-id input-id]]
   (get-in db (path->input form-id input-id))))

(rf/reg-sub
 (luna :dropdown-value)
 (fn [db [_ form-id input-id]]
   (get-in db (path->input form-id input-id))))

(rf/reg-sub
 (luna :search-value)
 (fn [db [_ form-id input-id]]
   (get-in db (path->input form-id input-id))))

(rf/reg-sub
 (luna :submitting?)
 (fn [db [_ form-id]]
   (get-in db (path->submit form-id :submitting?))))

(rf/reg-sub
 (luna :first-submit-dispatched?)
 (fn [db [_ form-id]]
   (get-in db (path->submit form-id :first-submit-dispatched?))))

(rf/reg-sub
 (luna :errors?)
 (fn [[evt form-id]]
   [(rf/subscribe [(luna :unprocessed-inputs) form-id])])
 (fn [[inputs]]
   (->> inputs
        (remove (fn [[_ {:keys [errors]}]] (empty? errors))))))

(rf/reg-sub
 (luna :input-errors)
 (fn [db [_ form-id input-id]]
   (get-in db (path->error form-id input-id))))

(rf/reg-sub
 (luna :server-message)
 (fn [db [_ form-id]]
   (get-in db (path->utils form-id :server-message))))

(rf/reg-sub
 (luna :notification?)
 (fn [db [_ form-id]]
   (get-in db (path->utils form-id :notification?))))

;; ------------ G E T  F O R M  V A L U E S ------------ ;;

;;; get form values in event from pure fn

(rf/reg-event-fx
 (luna :do-something-with-form)
 (fn [{:keys [db]} [_ form-id]]
   (pprint (collect-form-inputs
            (get-in db (path->form-id form-id))
            form-id))))

;; get form values from subscription

(rf/reg-sub
 (luna :form-inputs)
 (fn [[evt form-id]]
   [(rf/subscribe [(luna :unprocessed-inputs) form-id])])
 (fn [[inputs]]
   (collect-form-inputs-recursion inputs)))

;; ------------ E V E N T S ------------ ;;

(rf/reg-event-db
 (luna :set-input-value)
 (fn [db [_ form-id input-id input-value]]
   (if input-value
     (assoc-in db (path->input form-id input-id) input-value)
     (medley/dissoc-in db (path->input form-id input-id)))))

(rf/reg-event-db
 (luna :add-input-error)
 (fn [db [_ form-id input-id error]]
   (update-in db (path->error form-id input-id) (fnil conj #{}) error)))

(rf/reg-event-db
 (luna :remove-input-error)
 (fn [db [_ form-id input-id error]]
   (update-in db (path->error form-id input-id) disj error)))

(rf/reg-event-db
 (luna :set-submitting?)
 (fn [db [_ form-id value]]
   (assoc-in db (path->submit form-id :submitting?) value)))

(rf/reg-event-db
 (luna :set-first-submit-dispatched?)
 (fn [db [_ form-id value]]
   (assoc-in db (path->submit form-id :first-submit-dispatched?) value)))

;; data shape accepted:
;; {:name "foo" :email "bar" ...}

(rf/reg-event-db
 (luna :populate-form)
 (fn [db [_ form-id form-data]]
   (reduce
    (fn [new-state [input-id input-value]]
      (assoc-in new-state
                (path->input form-id input-id) input-value))
    db form-data)))

(rf/reg-event-db
 (luna :add-server-message)
 (fn [db [_ form-id message]]
   (assoc-in db (path->utils form-id :server-message) message)))

(rf/reg-event-db
 (luna :remove-server-message)
 (fn [db [_ form-id]]
   (medley/dissoc-in db (path->utils form-id :server-message))))

(rf/reg-event-db
 (luna :show-notification)
 (fn [db [_ form-id]]
   (assoc-in db (path->utils form-id :notification?) true)))

(rf/reg-event-db
 (luna :hide-notification)
 (fn [db [_ form-id]]
   (medley/dissoc-in db (path->utils form-id :notification?))))

;; ------------ E V E N T S / S U B .  S T Y L E ------------ ;;

(rf/reg-event-db
 (luna :store-icons)
 (fn [db [_ form-id icons]]
   (assoc-in db (path->utils form-id :icons) icons)))

(rf/reg-sub
 (luna :icons)
 (fn [db [_ form-id]]
   (get-in db (path->utils form-id :icons))))

(rf/reg-sub
 (luna :classes-input)
 (fn [[evt form-id input-id]]
   [(rf/subscribe [(luna :first-submit-dispatched?) form-id])
    (rf/subscribe [(luna :submitting?) form-id])
    (rf/subscribe [(luna :input-errors) form-id input-id])
    (rf/subscribe [(luna :icons) form-id])])
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
 (luna :utils-submit)
 (fn [[evt form-id]]
   [(rf/subscribe [(luna :first-submit-dispatched?) form-id])
    (rf/subscribe [(luna :submitting?) form-id])
    (rf/subscribe [(luna :errors?) form-id])])
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
 (luna :simulate-api-server-response)
 (fn [{:keys [db]} [_ form-id]]
   {:dispatch-n [[(luna :set-submitting?) form-id false]
                 [(luna :set-first-submit-dispatched?) form-id false]]}))

(rf/reg-event-fx
 (luna :test-submit)
 (fn [_ [_ form-id]]
   {:dispatch-later [{:ms 1000
                      :dispatch [(luna :simulate-api-server-response) form-id]}]}))
