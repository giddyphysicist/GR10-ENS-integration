(ns my-re-frame.subs
  (:require [re-frame.core :as re-frame]))



(re-frame/reg-sub
 ::addr
 (fn [db]
   (:addr db)))

(re-frame/reg-sub
 ::w3
 (fn [db]
   (:w3 db)))

(re-frame/reg-sub 
 ::addrmap
 (fn [db]
   (:addrmap (:ens db))))

(re-frame/reg-sub
 ::get-name-from-addrmap
 (fn [db addr]
   (let [addrmap (:addrmap (:ens db))
         name (get addrmap addr)]
     name
     )))
