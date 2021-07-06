(ns my-re-frame.events
  (:require [re-frame.core :as re-frame]
            
            [my-re-frame.db :as db]
            [my-re-frame.subs :as mysubs]

            [cljsjs.web3] ; You only need this, if you don't use MetaMask extension or Mist browser
            [district0x.re-frame.web3-fx]
            [cljs-web3.core :as web3]
            [cljs-web3.eth :as web3-eth]
            [district.ui.component.active-account]
            [clojure.string :as string]))

;; -------------------------
;; Utility Functions and Web3



(def ensAbi "[{\"constant\": true, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}], \"name\": \"resolver\", \"outputs\": [{\"name\": \"\", \"type\": \"address\"}], \"payable\": false, \"type\": \"function\"}, {\"constant\": true, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}], \"name\": \"owner\", \"outputs\": [{\"name\": \"\", \"type\": \"address\"}], \"payable\": false, \"type\": \"function\"}, {\"constant\": false, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}, {\"name\": \"label\", \"type\": \"bytes32\"}, {\"name\": \"owner\", \"type\": \"address\"}], \"name\": \"setSubnodeOwner\", \"outputs\": [], \"payable\": false, \"type\": \"function\"}, {\"constant\": false, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}, {\"name\": \"ttl\", \"type\": \"uint64\"}], \"name\": \"setTTL\", \"outputs\": [], \"payable\": false, \"type\": \"function\"}, {\"constant\": true, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}], \"name\": \"ttl\", \"outputs\": [{\"name\": \"\", \"type\": \"uint64\"}], \"payable\": false, \"type\": \"function\"}, {\"constant\": false, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}, {\"name\": \"resolver\", \"type\": \"address\"}], \"name\": \"setResolver\", \"outputs\": [], \"payable\": false, \"type\": \"function\"}, {\"constant\": false, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}, {\"name\": \"owner\", \"type\": \"address\"}], \"name\": \"setOwner\", \"outputs\": [], \"payable\": false, \"type\": \"function\"}, {\"anonymous\": false, \"inputs\": [{\"indexed\": true, \"name\": \"node\", \"type\": \"bytes32\"}, {\"indexed\": false, \"name\": \"owner\", \"type\": \"address\"}], \"name\": \"Transfer\", \"type\": \"event\"}, {\"anonymous\": false, \"inputs\": [{\"indexed\": true, \"name\": \"node\", \"type\": \"bytes32\"}, {\"indexed\": true, \"name\": \"label\", \"type\": \"bytes32\"}, {\"indexed\": false, \"name\": \"owner\", \"type\": \"address\"}], \"name\": \"NewOwner\", \"type\": \"event\"}, {\"anonymous\": false, \"inputs\": [{\"indexed\": true, \"name\": \"node\", \"type\": \"bytes32\"}, {\"indexed\": false, \"name\": \"resolver\", \"type\": \"address\"}], \"name\": \"NewResolver\", \"type\": \"event\"}, {\"anonymous\": false, \"inputs\": [{\"indexed\": true, \"name\": \"node\", \"type\": \"bytes32\"}, {\"indexed\": false, \"name\": \"ttl\", \"type\": \"uint64\"}], \"name\": \"NewTTL\", \"type\": \"event\"}]")
(def resolverAbi "[{\"constant\": true, \"inputs\": [{\"name\": \"interfaceID\", \"type\": \"bytes4\"}], \"name\": \"supportsInterface\", \"outputs\": [{\"name\": \"\", \"type\": \"bool\"}], \"payable\": false, \"type\": \"function\"}, {\"constant\": true, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}, {\"name\": \"contentTypes\", \"type\": \"uint256\"}], \"name\": \"ABI\", \"outputs\": [{\"name\": \"contentType\", \"type\": \"uint256\"}, {\"name\": \"data\", \"type\": \"bytes\"}], \"payable\": false, \"type\": \"function\"}, {\"constant\": false, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}, {\"name\": \"x\", \"type\": \"bytes32\"}, {\"name\": \"y\", \"type\": \"bytes32\"}], \"name\": \"setPubkey\", \"outputs\": [], \"payable\": false, \"type\": \"function\"}, {\"constant\": true, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}], \"name\": \"content\", \"outputs\": [{\"name\": \"ret\", \"type\": \"bytes32\"}], \"payable\": false, \"type\": \"function\"}, {\"constant\": true, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}], \"name\": \"addr\", \"outputs\": [{\"name\": \"ret\", \"type\": \"address\"}], \"payable\": false, \"type\": \"function\"}, {\"constant\": false, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}, {\"name\": \"contentType\", \"type\": \"uint256\"}, {\"name\": \"data\", \"type\": \"bytes\"}], \"name\": \"setABI\", \"outputs\": [], \"payable\": false, \"type\": \"function\"}, {\"constant\": true, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}], \"name\": \"name\", \"outputs\": [{\"name\": \"ret\", \"type\": \"string\"}], \"payable\": false, \"type\": \"function\"}, {\"constant\": false, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}, {\"name\": \"name\", \"type\": \"string\"}], \"name\": \"setName\", \"outputs\": [], \"payable\": false, \"type\": \"function\"}, {\"constant\": false, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}, {\"name\": \"hash\", \"type\": \"bytes32\"}], \"name\": \"setContent\", \"outputs\": [], \"payable\": false, \"type\": \"function\"}, {\"constant\": true, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}], \"name\": \"pubkey\", \"outputs\": [{\"name\": \"x\", \"type\": \"bytes32\"}, {\"name\": \"y\", \"type\": \"bytes32\"}], \"payable\": false, \"type\": \"function\"}, {\"constant\": false, \"inputs\": [{\"name\": \"node\", \"type\": \"bytes32\"}, {\"name\": \"addr\", \"type\": \"address\"}], \"name\": \"setAddr\", \"outputs\": [], \"payable\": false, \"type\": \"function\"}, {\"inputs\": [{\"name\": \"ensAddr\", \"type\": \"address\"}], \"payable\": false, \"type\": \"constructor\"}, {\"anonymous\": false, \"inputs\": [{\"indexed\": true, \"name\": \"node\", \"type\": \"bytes32\"}, {\"indexed\": false, \"name\": \"a\", \"type\": \"address\"}], \"name\": \"AddrChanged\", \"type\": \"event\"}, {\"anonymous\": false, \"inputs\": [{\"indexed\": true, \"name\": \"node\", \"type\": \"bytes32\"}, {\"indexed\": false, \"name\": \"hash\", \"type\": \"bytes32\"}], \"name\": \"ContentChanged\", \"type\": \"event\"}, {\"anonymous\": false, \"inputs\": [{\"indexed\": true, \"name\": \"node\", \"type\": \"bytes32\"}, {\"indexed\": false, \"name\": \"name\", \"type\": \"string\"}], \"name\": \"NameChanged\", \"type\": \"event\"}, {\"anonymous\": false, \"inputs\": [{\"indexed\": true, \"name\": \"node\", \"type\": \"bytes32\"}, {\"indexed\": true, \"name\": \"contentType\", \"type\": \"uint256\"}], \"name\": \"ABIChanged\", \"type\": \"event\"}, {\"anonymous\": false, \"inputs\": [{\"indexed\": true, \"name\": \"node\", \"type\": \"bytes32\"}, {\"indexed\": false, \"name\": \"x\", \"type\": \"bytes32\"}, {\"indexed\": false, \"name\": \"y\", \"type\": \"bytes32\"}], \"name\": \"PubkeyChanged\", \"type\": \"event\"}]")
(def ENS_MAINNET_ADDR "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e")
(def ZERO_HEX "0x0000000000000000000000000000000000000000000000000000000000000000")

(def ensAbiStruct (js/JSON.parse ensAbi))
(def resolverAbiStruct (js/JSON.parse resolverAbi))


(defn updateNode [node label]
  (web3/sha3 (str (subs node 2) (subs (web3/sha3 label) 2)) {:encoding :hex}))



(defn namehash [name]
  (def node (atom ZERO_HEX))
  (let [labels (rseq (string/split name "."))]
        
    (dotimes [n (count labels)] 
      (let [label (nth labels n)]
         (reset! node (updateNode @node label)))))
  @node)

  
(defn address-in-db? 
  [db addr]
    (contains? (:addrmap (:ens db)) addr))


(defn resolve-ens-name [addr]
  (try
  (let [w3 @(re-frame/subscribe [::mysubs/w3])
        reverse-addr-nh (namehash (str (string/lower-case (subs addr 2)) ".addr.reverse"))
        ensContract (web3-eth/contract-at w3 ensAbiStruct ENS_MAINNET_ADDR)
        resolverAddr (web3-eth/contract-call ensContract :resolver reverse-addr-nh)
        resolverContract (web3-eth/contract-at w3 resolverAbiStruct resolverAddr)
        ens-name (web3-eth/contract-call resolverContract :name reverse-addr-nh)
        ]
    
    (println (str "ENS NAME IN FUNCTION FOUND TO BE ... " ens-name))
    ens-name)
    (catch :default _ addr))
    )
 

;; -------------------------
;; Events

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::add-address-to-addrmap
 (fn [db [_ addr]]
   ;(println (str "TRYING TO ADD ADDRESS " addr " TO DB"))
   ;(println (str "OLD ADDRMAP DB: " (:addrmap (:ens db))))
   (if (not (string? addr)) 
     db
   (if (address-in-db? db addr)
     (if (string/blank? (get-in db [:ens :addrmap addr]))
       (let [ens-name (resolve-ens-name addr)]
         (assoc-in db [:ens :addrmap addr] ens-name))
        db)
     (let [ens-name (resolve-ens-name addr)]
       (assoc-in db [:ens :addrmap addr] ens-name))))))
     
       
   

(re-frame/reg-event-db
 ::update-addr
 (fn [db [_ new-addr]]
   (assoc db :addr new-addr)))


     