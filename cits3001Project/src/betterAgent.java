
import hanabAI.*;
import java.io.*;
import java.lang.*;
import java.util.*;
import java.awt.*;


public class betterAgent implements Agent{
	private int playerNum;
	//private int bestCard;
	private int player;
	private int cardsPlayable = 0;
	private int[] cardval;
	private Colour[] cardcol;
	private String[] data = new String[2];
	private boolean firstturn = true;
	private boolean[] save;
	private boolean[] playable; // whether or not a card is playable at the current state of the game.
	private ArrayList<String> cardHints = new ArrayList<String>(); //ints and cardcol (may have to be an arraylist
	//initialises all varibales and needed data
	public betterAgent(){
		//Contructor
	}
	public void initialise(State s){
		playerNum = s.getPlayers().length;
		System.out.println(player);
		if (playerNum == 3 || playerNum == 2){
			cardval = new int[5];
			cardcol = new Colour[5];
			playable  = new boolean[5];
			save  = new boolean[5];

		}else {
			cardval = new int[4];
			cardcol = new Colour[4];
			playable = new boolean[4];
			save  = new boolean[4];
		}
		player = s.getNextPlayer();
		firstturn = false;
	}
	public String toString(){
		return "Michael & Josh: Better Agent";
	}


	public Action doAction(State s){
		 if(firstturn){
		 		    initialise(s);
		 		  } 
		 		  //Assume players index is sgetNextPlayer()
		 		  player = s.getNextPlayer();
		 		  //get any hints
		 		  try{
		 		    getHints(s,player);
		 		    Action a = smartPlay(s);
		 		    //if(a==null) a = playKnown(s);
		 		    playableCards(s);
		 		    if(a == null) a = playKnown(s);
		 		    if(a==null) a = produceHint(s);
		 		    if(a==null) a = smartDiscard(s);
		 		    //if (cardsPlayable != 0){
		 		    //}
		 		    if(a==null) a = hint(s);
		 		    if(a==null) a = hintRandom(s);
		 		    if(a==null) a = discardKnown(s);
		 		    if(a==null) a = discardGuess(s);
		 		    //if(a==null) a = playGuess(s);
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
	//this should work.
	public void markSave(State s){
			for (int i =0; (i< playable.length) && cardcol[i]!= null; i++){
				if ((cardval[i]-1) == playable(s,cardcol[i])){
					save[i] = true;
				}
			}
	}

	public Action playSpecific(int card) throws IllegalActionException{
       cardcol[card] = null;
       cardval[card] = 0;
       return new Action(player, toString(), ActionType.PLAY,card);
	}

	public Action smartDiscard(State s) throws IllegalActionException{
		if(s.getHintTokens() == 8) return null;
		markSave(s);
		Stack<Card> discarded = new Stack<Card>();
		if(!(s.getDiscards().isEmpty())){ 
			discarded = s.getDiscards();
		}else{
			return null;
		}
		int size = discarded.size();
		if(size == 1){
			return null;
		}
		Card toCheck = discarded.pop();
		for (int i = 0; i<cardcol.length; i++){
			for (int j = 0; j < size; j++){
				if(!save[i] && !((cardval[i] == toCheck.getValue())&&(cardcol[i] == toCheck.getColour()))) {
					return new Action(player,toString(),ActionType.DISCARD,i);
			}
			}
		}
		return null;
	}

	/*
	Takes all the playable cards and calculates the potential "alpha" of playing them. It will prefer to play a card that will result in more playable cards on the next itereations
	Way the Method Works
		1. takes all the hands and checks the value and colour against the known values and colours of the current player
		2. 
	*/
	public Action smartPlay(State s) throws IllegalActionException{
		int best = 0;
		int bestCard = -1;
		Action a = null;
		for (int i = 0; i < cardcol.length && cardval[i] != 0 && cardcol[i] != null; i++){
			int alpha =0; 
			if (!(cardval[i] == playable(s,cardcol[i]))) continue;
			for (int j = 1; j<playerNum; j++){
				Card[] otherHand = s.getHand((player+i)%playerNum);
				for (int k =0; k < otherHand.length; k++){
					if(otherHand[k] == null) continue;
					if (otherHand[k].getColour() == cardcol[i]){
						if (otherHand[k].getValue() == cardval[i]+1){
							alpha += 2;
						}else if (otherHand[k].getValue() == cardval[i]+2){
							alpha += 1;
						}
					} else if (cardcol[i] == cardcol[k]){
						if (cardval[k] == cardval[i]+1){
							alpha += 2;
						}else if (cardval[k] == cardval[i]+2){
							alpha += 1;
						}
					}
				}
				if (alpha > best){
					bestCard = i;
					alpha = 0;
				}
			}
		} 
		if (bestCard != -1) a = playSpecific(bestCard);
		if (a == null) a = playKnown(s);
		return a;
	}
	


	public Action produceHint(State s) throws IllegalActionException{
		if(!(s.getHintTokens()>0)) return null;
		//State next = (State) s.clone();
		for (int i = 0; i<playerNum;i++){
			cardHints.clear();
			getHints(s,(player+i)%playerNum);
			Card[] hCheck = s.getHand((player+i)%playerNum);
			if(cardHints.isEmpty()){
				return null;
			}
			for(int j = 0; j<cardcol.length;j++){
				if(hCheck[j] == null)continue;
				if(!cardHints.contains(hCheck[j].getColour().toString()) && hCheck[j].getValue() == playable(s, hCheck[j].getColour())){
					return hintActual(s,"Colour",j);
				}else if (!cardHints.contains(hCheck[j].getValue()) && hCheck[j].getValue() == playable(s, hCheck[j].getColour()) ){
					return hintActual(s,"Value",j);
				}
			}
		}
		return null;		
	}

	public void playableCards(State s){
		cardsPlayable = 0;
		//Colour colourCurrent;
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


  public void getHints(State s, int target){
    try{
      State t = (State) s.clone();
      for(int i = 0; i<Math.min(playerNum-1,s.getOrder());i++){
        Action a = t.getPreviousAction();
        if((a.getType()==ActionType.HINT_COLOUR || a.getType() == ActionType.HINT_VALUE) && a.getHintReceiver()==target){
          boolean[] hints = t.getPreviousAction().getHintedCards();
          for(int j = 0; j<hints.length; j++){
            if(hints[j]){
              if(a.getType()==ActionType.HINT_COLOUR){
              	cardcol[j] = a.getColour();
              	if(!cardHints.contains(a.getColour().toString())) cardHints.add(a.getColour().toString());
              } 
              else if(a.getType()==ActionType.HINT_VALUE){
              	cardval[j] = a.getValue();
              	if(!cardHints.contains(Integer.toString(a.getValue()))) cardHints.add(Integer.toString(a.getValue()));
              }else if(a.getType()==ActionType.PLAY){
              		cardHints.remove(a.getColour().toString());
              		cardHints.remove(Integer.toString(a.getValue()));
              }else if(a.getType()==ActionType.DISCARD){
              		cardHints.remove(a.getColour().toString());
              		cardHints.remove(Integer.toString(a.getValue()));
              }                
            }
          }
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

    public Action hintActual(State s,String hint, int use) throws IllegalActionException{
    	System.out.println("GOODHINT");
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
  

}
