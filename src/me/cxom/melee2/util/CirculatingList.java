package me.cxom.melee2.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CirculatingList<E> {

	private List<E> e;
	private final boolean shuffle;
	private int i = -1;
	
	public CirculatingList(){
		this(false);
	}
	
	public CirculatingList(boolean shuffle){
		this(new ArrayList<E>());
	}
	
	public CirculatingList(E[] e){
		this(e, false);
	}
	
	public CirculatingList(E[] e, boolean shuffle){
		this(Arrays.asList(e), shuffle);
	}
	
	public CirculatingList(Collection<E> e){
		this(e, false);
	}
	
	public CirculatingList(Collection<E> e, boolean shuffle){
		this.e = new ArrayList<E>(e);
		this.shuffle = shuffle;
		if(shuffle) shuffle();
	}
	
	public E next() throws NullPointerException{
		if(e.size() == 0) throw new NullPointerException();
		if (i + 1 == e.size()){
			i = 0;
			if(shuffle) shuffle();
		}else{
			i++;
		}
		return e.get(i);
	}
	
	private void shuffle(){
		Collections.shuffle(e);
	}
	
	public List<E> getList(){
		return e;
	}
	
}
