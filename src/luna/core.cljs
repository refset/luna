(ns luna.core
  (:require
   [re-frame.core :refer [subscribe dispatch]]
   [luna.handlers :refer [<sub >evt]]
   [luna.validation :as validation]))

(defn input-core
  [type form-id input-id placeholder classes &
   [{:keys [on-change-input options]}]]
  (let [options-no-class (dissoc options :class)]
    [:input.input
     (merge
      {:type type
       :value (<sub [:luna/input-value form-id input-id])
       :class (cond-> (:input classes)
                (:class options) (str " " (:class options)))
       :placeholder placeholder
       :disabled (:disabled? classes)
       :on-change
       (fn [e]
         (let [value
               (-> e .-target .-value)
               new-input-value (if (seq value) value nil)]
           (>evt [:luna/set-input-value form-id input-id new-input-value])
           (when on-change-input (on-change-input))))}
      options-no-class)]))

(defn text-area-core
  [form-id input-id placeholder classes &
   [{:keys [on-change-input options]}]]
  (let [options-no-class (dissoc options :class)]
    [:textarea.textarea
     (merge
      {:value (<sub [:luna/input-value form-id input-id])
       :class (cond-> (:input classes)
                (:class options) (str " " (:class options)))
       :placeholder placeholder
       :disabled (:disabled? classes)
       :on-change
       (fn [e]
         (let [value
               (-> e .-target .-value)
               new-input-value (if (seq value) value nil)]
           (>evt [:luna/set-input-value form-id input-id new-input-value])
           (when on-change-input (on-change-input))))}
      options-no-class)]))

(defn input->basic
  [{:keys [form-id input-id label type validation placeholder] :as attrs}]
  (let [classes (<sub [:luna/classes-input form-id input-id])]
    [:div.field
     [:label.label label]
     [input-core type form-id input-id placeholder classes attrs]
     [:p.help
      {:class (:p classes)}
      (:error classes)]
     (validation/validate validation form-id input-id)]))

(defn input->pretty
  [{:keys [form-id icons]}]
  (>evt [:luna/store-icons form-id icons])
  (fn [{:keys [form-id input-id label type validation placeholder icons] :as attrs}]
    (let [classes (<sub [:luna/classes-input form-id input-id])]
      [:div.field
       [:label.label label]
       [:div.control.has-icons-left.has-icons-right
        [input-core type form-id input-id placeholder classes attrs]
        [:span.icon.is-small.is-left
         [:i {:class (str "fas " (:icon-left icons))}]]
        (when (:icon-right? icons)
          [:span.icon.is-small.is-right
           [:i {:class (:icon-right classes)}]])]
       [:p.help
        {:class (:p classes)} (:error classes)]
       (validation/validate validation form-id input-id)])))

(defn input->search
  [{:keys [form-id input-id label type placeholder on-click-button] :as attrs}]
  [:div.field.has-addons
   [:div.control
    [input-core type form-id input-id placeholder attrs]]
   [:div.control
    [:a.button.is-info
     {:on-click #(on-click-button)}
     label]]])

(defn dropdown
  [{:keys [form-id input-id input-vec]}]
  (>evt [:luna/set-input-value form-id input-id (first input-vec)])
  (fn [{:keys [form-id input-id input-vec on-change options]}]
    [:div.field
     [:div.control
      [:div.select options
       (into [:select
              {:on-click
               #(do
                  (>evt [:luna/set-input-value form-id input-id
                         (-> % .-target .-value)])
                  (when on-change (on-change)))}]
             (map (fn [i] [:option i]) input-vec))]]]))

(defn submit->form
  [{:keys [label form-id on-submit options]}]
  (let [{:keys [first-dispatched? button-classes submitting? errors disabled]}
        (<sub [:luna/utils-submit form-id])
        options-no-class (dissoc options :class)]
    (print disabled)
    [:div.field
     [:div.control
      [:button.button
       (merge
        {:class (str button-classes " " (:class options))
         :disabled disabled
         :on-click
         (fn [e]
           (.preventDefault e)
           (when-not submitting?
             (>evt [:luna/set-submitting? form-id true])
             (when-not first-dispatched?
               (>evt [:luna/set-first-submit-dispatched? form-id true]))
             (cond
               (empty? errors) (on-submit)
               :else (>evt [:luna/set-submitting? form-id false]))))}
        options-no-class)
       label]]]))

(defn text-area
  [{:keys [form-id input-id label validation placeholder] :as attrs}]
  (let [classes (<sub [:luna/classes-input form-id input-id])]
    [:div.field
     [:label.label label]
     [:div.control
      [text-area-core form-id input-id placeholder classes attrs]]
     [:p.help
      {:class (:p classes)}
      (:error classes)]
     (validation/validate validation form-id input-id)]))

(defn checkbox
  [{:keys [form-id input-id label options link?]}]
  [:div.field
   [:div.control
    [:label.checkbox
     [:input
      (merge
       {:type "checkbox"
        :on-change
        (fn [e]
          (>evt [:luna/set-input-value form-id input-id (-> e .-target .-checked)]))}
       options)]
     (str " " label " ")
     (when link?
       [:a {:href (:href link?)}
        (str (:label-on link?))])]]])

(defn button
  [{:keys [class label on-click options]}]
  [:div.field
   [:a.button {:class class
               :on-click #(on-click)}
    (when-let [icon (:icon-start? options)]
      [:span.icon
       [:i.fab {:class (:icon icon)}]])
    [:span label]
    (when-let [icon (:icon-end? options)]
      [:span.icon
       [:i.fab {:class (:icon icon)}]])]])

(defn notification
  [{:keys [form-id text options]}]
  (when (<sub [:luna/notification? form-id])
    [:div.notification options
     [:button.delete
      {:on-click #(>evt [:luna/hide-notification form-id])}]
     text]))
