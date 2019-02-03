# Luna 1.0  <img src="https://i.imgur.com/fAZcBXq.png" alt="alt text" width="40">

Luna is a light-weight ClojureScript **form management tool** that allows you to quickly build real time validation forms for web apps. 
Luna is a free open source project created using [re-frame](https://github.com/Day8/re-frame) and [reagent](https://github.com/reagent-project/reagent).

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
             :sha "e728a25823e7865b57aaf7433eab314e37449b2b"}
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

## Basic Input
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

### Mandatory values:

1. The values `form-id` and `input-id` ensure that the input is uniquely identifiable and that it belongs to one specific form.
If we were to have a second form element such as an email input, it would have had the same `form-id` and different `input-id` like `:email`.
2. A nested `input-id` is allowed and must be provided in a sequence such as `[:first-year :return]`. This will be represented in the state in a map structure like this one `{:first-year {:return ..}}`.
3. The `type` value is an attribute of the input tag and it can be `text`, `number`, or any other supported one.

### Optional values:

1. `label` and `placeholder` are meant to help the user understand what the input is about.
2. The input accepts none, one, or many `validation` functions. The `:valid?` key expects an anonymous function that is validated against `%` being the input value. 
Along with the validation, an `error` message must be provided, which will display right below the input.
3. Any valid tag can be attached to the input element via the `:option` key. Absolutely do not provide an `on-change` tag in here.
4. If you want to add a custom behaviour for the `on-change` tag, do it by passing a function via the `:on-change-input` key.

