
import hanabAI.*;
import java.io.*;
import java.lang.*;
import java.util.*;
import java.awt.*;


public class myAgent implements Agent{
	private int playerNum;
	private int player;
	private int cardsPlayable = 0;
	private int[] cardval;
	private Colour[] cardcol;
	private int[] cardvalOthers;
	private Colour[] cardcolOthers;
	private String[] data = new String[2];
	private boolean firstturn = true;
	private boolean[] playable; // whether or not a card is playable at the current state of the game.
	private ArrayList<String> cardHints = new ArrayList<String>(); //ints and cardcol (may have to be an arraylist)
	//initialises all varibales and needed data
	public myAgent(){
		//Contructor
	}
	public void initialise(State s){
		playerNum = s.getPlayers().length;
		System.out.println(player);
		if (playerNum <= 3){
			cardval = new int[5];
			cardcol = new Colour[5];
			cardvalOthers = new int[5*(playerNum-1)];
			cardcolOthers = new Colour[5*(playerNum-1)];
		} 
		else {
			cardval = new int[4];
			cardcol = new Colour[4];
			cardvalOthers = new int[4*(playerNum-1)];
			cardcolOthers = new Colour[4*(playerNum-1)];
		}
		player = s.getNextPlayer();
		firstturn = false;
	}
	public String toString(){
		return "Michael & Josh: Agent 1";
	}


	public Action doAction(State s){
		  if(firstturn){
		    initialise(s);
		  } 
		  //Assume players index is sgetNextPlayer()
		  player = s.getNextPlayer();
		  //get any hints
		  try{
		    getHints(s);
		    Action a = playKnown(s);
		    //playableCards(s);
		    //if (cardsPlayable != 0){
		    	//if(a==null) a = produceHint(s);
		    //}
		    if(a==null) a = hintSmart(s);
		    if(a==null) a = discardKnown(s);
		    if(a==null) a = hintRandom(s);
		    if(a==null) a = discardGuess(s);
		    if(a==null) a = playGuess(s);
		    return a;
		  }
		  catch(IllegalActionException e){
		    e.printStackTrace();
		    throw new RuntimeException("Something has gone very wrong");
		  }
		}
	/*
	mutates playable array to tell the doAction what cards are playable, and also counts them
	*/
	public Action produceHint(State s) throws IllegalActionException{
		State next = (State) s.clone();
		for (int i = 0; i<playerNum;i++){
			next.getNextPlayer();
			getHints(next);
			Card[] hCheck = s.getHand((player+i)%playerNum);
			if(cardHints.isEmpty()){
				return null;
			}
			for(int j = 0; j<cardcol.length;i++){
				if(hCheck[j] == null)continue;
				if(!cardHints.contains(hCheck[j].getColour().toString())){
					return hintActual(s,"Colour",j);
				}else if (!cardHints.contains(hCheck[j].getValue())){
					return hintActual(s,"Value",j);
				}
			}
		}
		return null;		
	}

	public void playableCards(State s){
		cardsPlayable = 0;
		Colour colourCurrent;
		for (int i =0; (i< playable.length) && cardcol[i]!= null; i++){
			if((playable(s,cardcol[i])) == cardval[i]){
				playable[i] = true;
				cardsPlayable++;
			}

		}

	}

	// with this method, make it check for both colour and name in internal if - also, change output so it can be figured out which one
	//similar to hint(), but doesnt randomly decide on the type of hint		return a;

	public void trackturns(State s, int current) throws IllegalActionException{
		State track = (State) s.clone();
		for (int i = s.getOrder(); i>0;i--){
			Action a = track.getPreviousAction();
			if (a.getHintReceiver() == current){
				boolean[] hints = track.getPreviousAction().getHintedCards();
				for(int j = 0; j<hints.length; j++){
				  if(hints[j]){
				    if(a.getType()==ActionType.HINT_COLOUR){
				    	cardHints.add(a.getColour().toString());
				    	//System.out.println(a.getColour().toString());
				    }  
				    else{
				      cardHints.add(Integer.toString(a.getValue()));  
				      //System.out.println(Integer.toString(a.getValue()));
				    }
				}
				  }
				}
			}
		}
/*
code from this point onward is from BasicAgent.java
@author Tim French
*/


  public void getHints(State s){
    try{
      State t = (State) s.clone();
      for(int i = 0; i<Math.min(playerNum-1,s.getOrder());i++){
        Action a = t.getPreviousAction();
        if((a.getType()==ActionType.HINT_COLOUR || a.getType() == ActionType.HINT_VALUE)){
	        if(a.getHintReceiver()==player) { 
	            boolean[] hints = t.getPreviousAction().getHintedCards();
	            for(int j = 0; j<hints.length; j++){
	        	    if(hints[j]){
		              if(a.getType()==ActionType.HINT_COLOUR){
		              	cardcol[j] = a.getColour();
		              } 
		              else{
		              	cardval[j] = a.getValue();
		              }                
	                }
	            }
	        }
	        else {
	        	
	        	int otherPlayerInt = a.getHintReceiver();
	            int relativeIndex = (otherPlayerInt + player)%playerNum -1;
	            int start = relativeIndex*cardval.length;
	        	boolean[] hints = t.getPreviousAction().getHintedCards();
	        	for(int j = start; j < start+cardval.length; j++) {
	        		if(hints[j]) {
	  		            if(a.getType()==ActionType.HINT_COLOUR){
	  		              	cardcolOthers[j] = a.getColour();
	  		            } 
	  		              else{
	  		              	cardvalOthers[j] = a.getValue();
	  		            }                
	  	               
	        		}
	        	}
	        		
	        }
        }
        else {
    		int otherPlayerInt = a.getPlayer();
    		int posOfCards = a.getCard();
        	int relativeIndexOfCard = (otherPlayerInt + player + playerNum)%playerNum -1;
        	cardcolOthers[(relativeIndexOfCard*cardval.length)+posOfCards] = null;
        	cardvalOthers[(relativeIndexOfCard*cardval.length)+posOfCards] = 0;
        }
        t = t.getPreviousState();
      }
    }
    catch(IllegalActionException e){e.printStackTrace();}
  }

  //returns the value of the next playable card of the given colour
  public int playable(State s, Colour c){
    java.util.Stack<Card> fw = s.getFirework(c);
    if (fw.size()==5) return -1;
    else return fw.size()+1;
  }

  //plays the first card known to be playable.
  public Action playKnown(State s) throws IllegalActionException{
    for(int i = 0; i<cardcol.length; i++){
      if(cardcol[i]!=null && cardval[i]==playable(s,cardcol[i])){
        cardcol[i] = null;
        cardval[i] = 0;
        return new Action(player, toString(), ActionType.PLAY,i);
      }
    }
    return null;
  }

  //discards the first card known to be unplayable.
  public Action discardKnown(State s) throws IllegalActionException{
    if (s.getHintTokens() != 8) {
      for(int i = 0; i<cardcol.length; i++){
        if(cardcol[i]!=null && cardval[i]>0 && cardval[i]<playable(s,cardcol[i])){
          cardcol[i] = null;
          cardval[i] = 0;
          return new Action(player, toString(), ActionType.DISCARD,i);
        }
      }
    }
    return null;
  }

  //gives hint of first playable card in next players hand
  //flips a coin to determine whether it is a colour hint or value hint
  //return null if no hint token left, or no playable cards
  //hint method is altered, so that it checks whether or not an element has been hinted before
  public Action hint(State s) throws IllegalActionException{
      if(s.getHintTokens()>0){
        for(int i = 1; i<playerNum; i++){
          int hintee = (player+i)%playerNum;
          Card[] hand = s.getHand(hintee);
          for(int j = 0; j<hand.length; j++){
            Card c = hand[j];
            if(c!=null && c.getValue()==playable(s,c.getColour())){
              //flip coin
              if(Math.random()>0.5){//give colour hint
                boolean[] col = new boolean[hand.length];
                for(int k = 0; k< col.length; k++){
                  col[k]=c.getColour().equals((hand[k]==null?null:hand[k].getColour()));
                }
                return new Action(player,toString(),ActionType.HINT_COLOUR,hintee,col,c.getColour());
              }
              else{//give value hint
                boolean[] val = new boolean[hand.length];
                for(int k = 0; k< val.length; k++){
                  val[k]=c.getValue() == (hand[k]==null?-1:hand[k].getValue());
                }
                return new Action(player,toString(),ActionType.HINT_VALUE,hintee,val,c.getValue());
              }
            }
          }
        }
      }
      return null;
    }
  
    public Action hintSmart(State s) throws IllegalActionException{
    	if(s.getHintTokens()>0){
            ArrayList<Card> nextPlayable = new ArrayList<Card>();
            for(Colour c: Colour.values()) {
          	  nextPlayable.add(s.getFirework(c).peek());
          	  System.out.println("test");
            }
            PriorityQueue<Action> bestHint = new PriorityQueue<Action>();
	    	for(int i = 1; i<playerNum; i++){
	            int hintee = (player+i)%playerNum;
	            Card[] hand = s.getHand(hintee);
	            for(int j = 0; j<hand.length; j++){
	              Card c = hand[j];
	              if(c!=null && c.getValue()==playable(s,c.getColour())){
	                //flip coin
	                if(Math.random()>0.5){//give colour hint
	                  boolean[] col = new boolean[hand.length];
	                  for(int k = 0; k< col.length; k++){
	                    col[k]=c.getColour().equals((hand[k]==null?null:hand[k].getColour()));
	                  }
	                  return new Action(player,toString(),ActionType.HINT_COLOUR,hintee,col,c.getColour());
	                }
	                else{//give value hint
	                  boolean[] val = new boolean[hand.length];
	                  for(int k = 0; k< val.length; k++){
	                    val[k]=c.getValue() == (hand[k]==null?-1:hand[k].getValue());
	                  }
	                  return new Action(player,toString(),ActionType.HINT_VALUE,hintee,val,c.getValue());
	                }
	              }
	            }
	          }
    	}
    	return null;
    }

    public Action hintActual(State s,String hint, int use) throws IllegalActionException{
        if(s.getHintTokens()>0){
          for(int i = 1; i<playerNum; i++){
            int hintee = (player+i)%playerNum;
            Card[] hand = s.getHand(hintee);
            for(int j = 0; j<hand.length; j++){
              Card c = hand[j];
              if(c!=null && use==playable(s,c.getColour())){
                //flip coin
                if(hint == "Colour"){//give colour hint
                  boolean[] col = new boolean[hand.length];
                  for(int k = 0; k< col.length; k++){
                    col[k]=c.getColour().equals((hand[k]==null?null:hand[k].getColour()));
                  }
                  return new Action(player,toString(),ActionType.HINT_COLOUR,hintee,col,c.getColour());
                }
                else{//give value hint
                  boolean[] val = new boolean[hand.length];
                  for(int k = 0; k< val.length; k++){
                    val[k]=c.getValue() == (hand[k]==null?-1:hand[k].getValue());
                  }
                  return new Action(player,toString(),ActionType.HINT_VALUE,hintee,val,c.getValue());
                }
              }
            }
          }
        }
        return null;
      }

  //with probability 0.05 for each fuse token, play a random card 
  public Action playGuess(State s) throws IllegalActionException{
  	System.out.println("rand");
  	System.out.println();
  	System.out.println();
    java.util.Random rand = new java.util.Random();
    for(int i = 0; i<s.getFuseTokens(); i++){
      if(rand.nextDouble()<0.05){
        int cardIndex = rand.nextInt(cardcol.length);
        cardcol[cardIndex] = null;
        cardval[cardIndex] = 0;
        return new Action(player, toString(), ActionType.PLAY, cardIndex);
      }
    }
    return null;
  }
  
  //discard a random card
  public Action discardGuess(State s) throws IllegalActionException{
    if (s.getHintTokens() != 8) {
      java.util.Random rand = new java.util.Random();
      int cardIndex = rand.nextInt(cardcol.length);
      cardcol[cardIndex] = null;
      cardval[cardIndex] = 0;
      return new Action(player, toString(), ActionType.DISCARD, cardIndex);
    }
    return null;
  }

  //gives random hint of a card in next players hand
  //flips a coin to determine whether it is a colour hint or value hint
  //return null if no hint token left
  public Action hintRandom(State s) throws IllegalActionException{
    if(s.getHintTokens()>0){
        int hintee = (player+1)%playerNum;
        Card[] hand = s.getHand(hintee);

        java.util.Random rand = new java.util.Random();
        int cardIndex = rand.nextInt(hand.length);
        while(hand[cardIndex]==null) cardIndex = rand.nextInt(hand.length);
        Card c = hand[cardIndex];

        if(Math.random()>0.5){//give colour hint
          boolean[] col = new boolean[hand.length];
          for(int k = 0; k< col.length; k++){
            col[k]=c.getColour().equals((hand[k]==null?null:hand[k].getColour()));
          }
          return new Action(player,toString(),ActionType.HINT_COLOUR,hintee,col,c.getColour());
        }
        else{//give value hint
          boolean[] val = new boolean[hand.length];
          for(int k = 0; k< val.length; k++){
            if (hand[k] == null) continue;
            val[k]=c.getValue() == (hand[k]==null?-1:hand[k].getValue());
          }
          return new Action(player,toString(),ActionType.HINT_VALUE,hintee,val,c.getValue());
        }

      }
    
    return null;
  }
  
  public static void main(String[] args) {
	  myAgent test = new myAgent();
	  
  }

}
