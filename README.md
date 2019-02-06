# Luna 1.0  <img src="https://i.imgur.com/fAZcBXq.png" alt="alt text" width="40">

Luna is a light-weight ClojureScript **form management tool** that allows you to quickly build real time validation forms for web apps. 
Luna is a free open source project created using [re-frame](https://github.com/Day8/re-frame) and [reagent](https://github.com/reagent-project/reagent).

 <img src="https://imgur.com/DkCAfrf.png" alt="alt" width="600">
 
Author: *Lucio D'Alessandro*

## Showcase Demo

<img src="https://mainefamilyplanning.org/wp-content/uploads/2018/04/Coming-Soon-PNG.png" alt="alt text" width="200">

## Setup & Installation

1. Add the following dependecy in your `deps.edn` file:
```
:build {
    {:extra-deps {
        luciodale/luna
            {:git/url "https://github.com/luciodale/luna"
             :sha "6b60c79e8ace809ad4db7f20acefa30516e0abb4"}
             ...
        }
     }
```
2. Require `[luna.core :as luna]` in your namespace
3. Download the CSS framework [BULMA](https://bulma.io/documentation/overview/start/) or simply require the following in your HTML `<head>` tag:
```
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bulma/0.7.2/css/bulma.min.css">
<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.7.0/css/all.css" integrity="sha384-lZN37f5QGtY3VHgisS14W3ExzMWZxybE1SJSEsQp9S+oqd12jhcu+A56Ebc1zFSJ" crossorigin="anonymous">
```

## API Documentation
<img src="https://cdn4.iconfinder.com/data/icons/eldorado-weather/40/thunder-512.png" alt="alt text" width="20"> Be ready to build your forms with no overhead! 

*N.B. the`rf/` namespace used below stands for `re-frame.core`*

### Basic Input
```
 [luna/input->basic
     {
       
      ;; mandatory values
      
      :form-id :login
      :input-id :name
      :type "text"
      
      ;; optional values
      
      :label "Name" 
      :placeholder "Luna"
      :validation
      [{:valid? #(some? %)
        :error "The input cannot be empty"}
       {:valid? #(> (count %) 3)
        :error "Your name must be longer than 3 chars"}]
      :options {:class "any-class" :id "any id"}
      :on-change-input #(print "pass your custom on-change handler")}]
```

#### Mandatory values:

1. The values `form-id` and `input-id` ensure that the input is uniquely identifiable and that it belongs to one specific form.
If we were to have a second form element such as an email input, it would have had the same `form-id` and different `input-id` like `:email`.
2. A nested `input-id` is allowed and must be provided in a sequence such as `[:first-year :return]`. This will be represented in the state in a map structure like this one `{:first-year {:return ..}}`.
3. The `type` value is an attribute of the input tag and it can be `text`, `number`, or any other supported one.

#### Optional values:

1. `label` and `placeholder` are meant to help the user understand what the input is about.
2. The input accepts none, one, or many `validation` functions. The `:valid?` key expects an anonymous function that is validated against `%` being the input value. 
Along with the validation, an `error` message must be provided, which will display right below the input.
3. Any valid tag can be attached to the input element via the `:option` key. Absolutely do not provide an `on-change` tag in here.
4. If you want to add a custom behaviour for the `on-change` tag, do it by passing a function via the `:on-change-input` key.

### Pretty Input
```
  [luna/input->pretty
     {
      
      ;; everything of the basic input plus more!
      
      :icons {:icon-left "fa-user"
              :icon-right? true
              :icon-right-success "fa-check fa-xs"
              :icon-right-danger "fa-exclamation-triangle fa-xs"}}]
```

#### Icons:

1. The pretty input adds some nice icons to its layout. It supports only a left icon or both a left and a right icon.
2. The right icon will appear only after the first sumbit is dispatched (and when some errors occur). Also, note that `:icon-right?` must be true to add the right icon functionality to the input. 
3. To know all the icons available, you can look at the font awesome 5 documentation.

### Dropdown
```
 [luna/dropdown
     {:form-id :login
      :input-id :dropdown
      :input-vec ["option1" "option2" "option3"]
        
      ;; optional values
     
      :on-change #(print "dispatch your custom event here")
      :options {:class "my-class" :id "my-id"}}]
```

### Checkbox
```
[luna/checkbox
     {:form-id :login
      :input-id :checkbox
      
      ;; optional values
      
      :label "Checkbox"
      :link? {:href "www.google.com"
              :label-on "The link falls on this text"}
      :options {...}}]
```

### Text Area
```
[luna/text-area
     {:form-id :login
      :input-id :text-area
      
      ;; optional values
      
      :label "Text Area"
      :placeholder "Any Placeholder"
      :validation {:valid? #(some? %)
                   :error "The text area cannot be empty"}
      :options {...}}]
```

### Input Search
```
[luna/input->search
     {:form-id :login
      :input-id :input-search
      :label "Input Search"
      :type "text"
      :placeholder "Search"
      :on-click-button #(print "Handler on button click")
      :on-change-input #(...)
      :options {}}]
```

### Simple Button
```
[luna/button
     {:class "is-primary"
      :label "Simple Button"
      :on-click #(print "Button handler")
      :options {:icon-start? {:icon "fa-twitter"}
                :icon-end? {:icon "fa-github"}}}]
```

#### Brief Overview:
These inputs do not require much explanation, as they are easy to understand from the functions themselves.

### Submit Button
```
[luna/submit->form
     {:form-id :login
      :label "Submit"
      :on-submit #(do
                    (rf/dispatch [:luna/show-notification :login])
                    (print "Your custom handler"))
      :options {...}}]
```

#### On Submit Actions
1. A notification can be shown on submit by dispatching the event `(rf/dispatch [:luna/show-notification :login])`.
2. Any other custom event can be dispatched from the same `on-submit` event.

### Notification
```
 [luna/notification
     {:form-id :login
      :text "Your notification text"
      :options {:class "is-primary"}}]

```

#### Usage
The notification can be customized via the `:options` key. When the `x` top-right icon is clicked, the notification closes automatically.  

## Collect Input Values
Two re-frame examples will be provided to show how the form values can be retrieved.

### Simple helper function in EVENT
```
(rf/reg-event-fx
 :submit-dummy-handler
 (fn [{:keys [db]} [_ form-id]]
   (luna/collect-form-inputs
         db
         form-id))))
```

#### Arguments
1. The first argument is simply the state. 
2. The second argument is the form-id
3. This function will return the input values in the following flat structure: 
`{:name "Luna" :email "luna@luna.luna" ...}`

### Nested Subscription
```
(rf/reg-sub
 :all-form-inputs
 (fn [[_ form-id]]
   [(rf/subscribe [:luna/unprocessed-inputs form-id])])
 (fn [[inputs]]
   (luna/collect-form-inputs-recursion inputs)))
```

#### Flow
1. The first subscription returns the input values in a non-polished format.
2. The variable called `inputs` holds the non-polished values, which are later passed to
`collect-form-inputs-recursion` to extract only the meaningful bits.

### More on other re-frame handlers soon!
