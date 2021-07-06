(ns my-re-frame.db
  (:require [cljs-web3.core :as web3]))

(def default-db
  {
   :addr "0xNOTaRESOLVEDaddress1111111111111111111111"
   :w3 (web3/create-web3 "https://mainnet.infura.io/v3/<INFURA_API_KEY_HERE>")
   :ens {:addrmap {}}
   })
