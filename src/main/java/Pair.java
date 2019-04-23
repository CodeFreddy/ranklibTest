package main.java;

import java.io.Serializable;


 public final class Pair<H, T> implements Serializable {
        private H key;
        private T value;

        public static <U, V> Pair<U, V> make(U key, V value){
            return new Pair<>(key, value);
        }
        //Constructor
        public Pair(H head, T tail){
            this.key = head;
            this.value = tail;
        }

        public H getKey() {
            return key;
        }

        public void setKey(H head) {
            this.key = head;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T tail) {
            this.value = tail;
        }

        @Override
        public String toString(){
            return "["+key.toString()+"]["+value.toString()+"]";
        }

        @Override
        public int hashCode(){
            return key.hashCode()+value.hashCode();
        }

        @Override
        public boolean equals(Object obj){
            Pair pairObj = (Pair)obj;
            return (key.equals(pairObj.getKey()) && value.equals(pairObj.getValue()));
        }
    }

