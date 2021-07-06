(ns my-re-frame.views
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [my-re-frame.subs :as subs]
            [my-re-frame.events :as events]
            [clojure.string :as string]))


(defn name-render [addr]
  (let [addrmap @(subscribe [::subs/addrmap])
        ens-name (if (contains? addrmap addr) (get addrmap addr) "")]
    
    (if (string/blank? ens-name)
      [:div {:on-click (fn [] (.clipboard/writeText js/navigator addr) (js/alert (str "Copied Address to Clipboard: " addr)))} addr]
      [:div {:on-click (fn [] (.clipboard/writeText js/navigator addr) (js/alert (str "Copied Address to Clipboard: " addr)))} ens-name])))



(defn ens-resolved-address
  [addr]
  (dispatch [::events/add-address-to-addrmap addr])
  (r/create-class                 ;; <-- expects a map of functions 
   {:display-name  "ens-resolved-address"      ;; for more helpful warnings & errors

    :component-did-mount               ;; the name of a lifecycle function
    (fn [addr]
      (dispatch [::events/add-address-to-addrmap addr])
    )
    
    :component-did-update
    (fn [addr ]
      (println "component-did-update")
      (dispatch [::events/add-address-to-addrmap addr])
    )

    :reagent-render        ;; Note:  is not :render
    (fn [addr ]           ;; remember to repeat parameters
      [:div
       [name-render addr]
       ])
    }
    ))


(defn main-panel []
  (let [addr (subscribe [::subs/addr])
        addrmap (subscribe [::subs/addrmap])
        ]
    [:div
     [:div {:style {:background-color "lightblue" :margin 20}}
     [:p "Address: " @addr]
     [:p "ENS-name: " (get @addrmap @addr)]
     ]
     [:p "RENDERED ADDRESS WITH NO ENS RECORD:"]
     [:div {:style {:background-color "#5d8aa8" :margin 20 :border "3px solid black" :border-radius 10 :cursor "pointer"}}
      [ens-resolved-address @addr]]
      [:div 
     [:button {:on-click #(dispatch [::events/update-addr "0x34aA3F359A9D614239015126635CE7732c18fDF3"] [::events/add-address-to-addrmap @addr])} "CHANGE ADDRESS!"]
     [:button  {:on-click #(dispatch [::events/add-address-to-addrmap @addr])} "Add Address to dB"]

     [:div " "]
      ]

     [:div 
      [:div {:style {:background-color "lightblue" :margin 20}}
       [:p "Address: " "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045"]
       [:p "ENS-name: " (get @addrmap "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045")]]
      [:p "RENDERED ADDRESS WITH ENS RECORD:"]
       [:div {:style {:background-color "#5d8aa8" :margin 20 :border "3px solid black" :border-radius 10 :cursor "pointer"}}
        [ens-resolved-address "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045"]]
    ]
     [:div {:style {:background-color "lightblue" :margin 20}}
      [:h3 "AddressMap: "]
      [:p (str @addrmap)]]]))





