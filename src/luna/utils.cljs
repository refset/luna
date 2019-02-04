(ns luna.utils)

;; ------------ N A M E S P A C E ------------ ;;

(defn luna
  [handler]
  (keyword (str "luna/" (name handler))))

(defn to->ns
  [form-id handler]
  (when form-id
    (keyword (str (name form-id) "/" (name handler)))))

;; ------------ P A T H S ------------ ;;

(defn- flatten->path
  [& path-seq]
  (into [] (flatten path-seq)))

(defn path->form-id
  [form-id]
  (flatten->path
   (luna :forms)
   form-id))

(defn path->inputs
  [form-id]
  (flatten->path
   (luna :forms)
   form-id
   (to->ns form-id :inputs)))

(defn path->input
  [form-id input-id]
  (flatten->path
   (luna :forms)
   form-id
   (to->ns form-id :inputs)
   input-id :value))

(defn path->error
  [form-id input-id]
  (flatten->path
   (luna :forms)
   form-id
   (to->ns form-id :inputs)
   input-id :errors))

(defn path->submit
  [form-id flag]
  (flatten->path
   (luna :forms)
   form-id
   (to->ns form-id :submit)
   flag))

(defn path->utils
  [form-id flag]
  (flatten->path
   (luna :forms)
   form-id
   (to->ns form-id :utils)
   flag))

;; ----------- Retrieve Form Values ----------- ;;

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
  [db form-id]
  (let [form-inputs (get-in db (path->inputs form-id))]
    (collect-form-inputs-recursion form-inputs)))
