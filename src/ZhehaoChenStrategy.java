import java.lang.reflect.*;
import java.util.*;

public class ZhehaoChenStrategy extends ComputerBattleshipPlayer
{	
	private enum Status
	{
		HUNT,
		TARGET;
	}
	
	private enum Parity
	{
		D (2, 'D'),
		C (3, 'C'),
		S (3, 'S'),
		B (4, 'B'),
		A (5, 'A');
		
		final char name;
		final int parity;
		Parity(int i, char c)
		{
			this.parity = i;
			this.name = c;
		}

	}
	
	private enum Direction
	{
		NONE,
		HORIZONTAL,
		VERTICAL;
	}
        
	private class TargetStack<E> extends ArrayList implements List, RandomAccess, Cloneable, java.io.Serializable
	{
		public boolean removePosition(Position p)
		{
			if(p == null)
			{
				for(int i = 0; i < super.size(); i++)
				{
					if(super.get(i) == null)
					{
						super.remove(i);
						return true;
					}
				}
			}
			else
			{
				for(int i = 0; i < super.size(); i++)
				{
					try
					{
						Method columnIndex = super.get(i).getClass().getDeclaredMethod("columnIndex");
						Method rowIndex =  super.get(i).getClass().getDeclaredMethod("rowIndex");
						Object columnI = columnIndex.invoke(super.get(i));
						Object rowI = rowIndex.invoke(super.get(i));
						int col = (Integer) columnI;
						int row = (Integer) rowI;
						if(p.columnIndex() == col && p.rowIndex() == row)
						{
							super.remove(i);
						}
					}
					catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
					{
						
					}
				}
			}
			return false;
		}
		
		public boolean push(E Position)
		{
			return super.add(Position);
		}
		
		public E pop()
		{
			return (E) super.remove(super.size() - 1);
		}
	}
	
	private Status status;
	private Parity parity;
	private char currentTarget = '\u0000';
	private Position firstHitA = null, firstHitB = null, firstHitC = null, firstHitS = null, firstHitD = null;
	private Direction directionA = Direction.NONE, directionB = Direction.NONE, directionC = Direction.NONE, directionS = Direction.NONE, directionD = Direction.NONE;
	private HashSet<Parity> stillAlive;
	private TargetStack<Position> targetStackA, targetStackB, targetStackC, targetStackS, targetStackD;
	BattleshipPlayer display = new BattleshipPlayer();
	
	public ZhehaoChenStrategy()
	{

	}
	
	@Override
	public void startGame()
	{
		super.initializeGrid();
		status = Status.HUNT;
		stillAlive = new HashSet();
		stillAlive.add(Parity.D);
		stillAlive.add(Parity.C);
		stillAlive.add(Parity.S);
		stillAlive.add(Parity.B);
		stillAlive.add(Parity.A);
		parity = Parity.D;
		targetStackA = new TargetStack();
		targetStackB = new TargetStack();
		targetStackC = new TargetStack();
		targetStackS = new TargetStack();
		targetStackD = new TargetStack();
		//debug
		display.initializeGrid();
	}
	
	@Override
	public String playerName()
	{
		return "Zhehao Chen Strategy";
	}
	
	public String author()
	{
		return "Zhehao Chen";
	}
	
	
	@Override
	public Position shoot()
	{
		Position target;
		if (status == Status.HUNT)
		{
			target = getNextHUNTTarget();
			return target;
		}
		else if(status == Status.TARGET)
		{
			target = getNextTARGETTarget();
			return target;
		}
		return null;
	}
	
	@Override
	public void updatePlayer(Position pos, boolean hit, char initial, String boatName, boolean sunk, boolean gameOver, boolean tooManyTurns, int turns)
	{
		super.updatePlayer(pos, hit, initial, boatName, sunk, gameOver, tooManyTurns, turns);
		display.updatePlayer(pos, hit, initial, boatName, sunk, gameOver, tooManyTurns, turns);
		parity = smallestParity();
		if(hit)
		{
			status = Status.TARGET;
			
			if(currentTarget == '\u0000')
			{
				currentTarget = initial;
			}
		}
		
		if(sunk)
		{
			stillAlive.remove(toParity(initial));
			currentTarget = straggler();
			if(currentTarget == '\u0000')
			{
				status = Status.HUNT;
			}
			else
			{
				status = Status.TARGET;
			}
		}
		manageStack(pos, hit, sunk, initial);
	}
	
	private Parity toParity(char c)
	{
		switch(c)
		{
			case 'A':
				return Parity.A;
			case 'B':
				return Parity.B;
			case 'C':
				return Parity.C;
			case 'S':
				return Parity.S;
			case 'D':
				return Parity.D;
			default:
				return null;
		}
	}
	
	private void manageStack(Position pos, boolean hit, boolean sunk, char initial)
	{
		int col = pos.columnIndex();
		int row = pos.rowIndex();
		if(!hit)
		{
			return;
		}
		
		if(sunk)
		{
			switch(initial)
			{
				case 'A':
					targetStackA = new TargetStack();
					break;
				case 'B':
					targetStackB = new TargetStack();
					break;
				case 'C':
					targetStackC = new TargetStack();
					break;
				case 'S':
					targetStackS = new TargetStack();
					break;
				case 'D':
					targetStackD = new TargetStack();
					break;
			}
		}
		
		//hit
		System.out.println(pos + " IS HIT");
		
		Position east = null;
		Position west = null;
		Position north = null;
		Position south = null;
		Direction direction = Direction.NONE;
		TargetStack<Position> temp = new TargetStack();
		
		switch(initial)
		{
			case 'A':
				if(firstHitA == null)
				{
					firstHitA = pos;
				}
				else if(directionA.equals(Direction.NONE))
				{
					if(pos.row() == firstHitA.row())
					{
						System.out.println(pos + " " + firstHitA + "HORIZONTAL");
						directionA = Direction.HORIZONTAL;
					}
					if(pos.column() == firstHitA.column())
					{
						System.out.println(pos + " " + firstHitA + "VERITCAL");
						directionA = Direction.VERTICAL;
					}
				}
				break;
			case 'B':
				if(firstHitB == null)
				{
					firstHitB = pos;
				}
				else if(directionB.equals(Direction.NONE))
				{
					if(pos.row() == firstHitB.row())
					{
						System.out.println(pos + " " + firstHitB + "HORIZONTAL");
						directionB = Direction.HORIZONTAL;
					}
					if(pos.column() == firstHitB.column())
					{
						System.out.println(pos + " " + firstHitB + "VERTICAL");
						directionB = Direction.VERTICAL;
					}
				}
				break;
			case 'C':
				if(firstHitC == null)
				{
					firstHitC = pos;
				}
				else if(directionC.equals(Direction.NONE))
				{
					if(pos.row() == firstHitC.row())
					{
						System.out.println(pos + " " + firstHitC + "HORIZONTAL");
						directionC = Direction.HORIZONTAL;
					}
					if(pos.column() == firstHitC.column())
					{
						System.out.println(pos + " " + firstHitC + "VERTICAL");
						directionC = Direction.VERTICAL;
					}
				}
				break;	
			case 'S':
				if(firstHitS == null)
				{
					firstHitS = pos;
				}
				else if(directionS.equals(Direction.NONE))
				{
					if(pos.row() == firstHitS.row())
					{
						System.out.println(pos+ " " + firstHitS + "HORIZONTAL");
						directionS = Direction.HORIZONTAL;
					}
					if(pos.column() == firstHitS.column())
					{
						System.out.println(pos + " " + firstHitS + "VERTICAL");
						directionS = Direction.VERTICAL;
					}
				}
				break;
			case 'D':
				if(firstHitD == null)
				{
					firstHitD = pos;
				}
				else if(directionD.equals(Direction.NONE))
				{
					if(pos.row() == firstHitD.row())
					{
						System.out.println(pos + " " + firstHitD + "HORIZONTAL");
						directionD = Direction.HORIZONTAL;
					}
					if(pos.column() == firstHitD.column())
					{
						System.out.println(pos + " " + firstHitD + "VERTICAL");
						directionD = Direction.VERTICAL;
					}
				}
				break;
		}
		
		if(col > 0)
		{
			west = new Position(row, col - 1);
			if(getGrid().empty(west))
			{
				temp.push(west);
			}
		}
		
		if(col < 9)
		{
			east = new Position(row, col + 1);
			if(getGrid().empty(east))
			{
				temp.push(east);
			}
		}
		
		if(row > 0)
		{
			north = new Position(row - 1, col);
			if(getGrid().empty(north))
			{
				temp.push(north);
			}
		}
		
		if(row < 9)
		{
			south = new Position(row + 1, col);
			if(getGrid().empty(south))
			{
				temp.push(south);
			}
		}
		
		switch(initial)
		{
			case 'A':
				if(stillAlive.contains(Parity.A))
				{
					targetStackA.addAll(temp);
				}
				break;
			case 'B':
				if(stillAlive.contains(Parity.B))
				{	
					targetStackB.addAll(temp);
				}
				break;
			case 'C':
				if(stillAlive.contains(Parity.C))
				{
					targetStackC.addAll(temp);
				}
				break;
			case 'S':
				if(stillAlive.contains(Parity.S))
				{
					targetStackS.addAll(temp);
				}
				break;
			case 'D':
				if(stillAlive.contains(Parity.D))
				{
					targetStackD.addAll(temp);
				}
				break;
		}
		
	}
	
	private Parity smallestParity()
	{
		Parity min = Parity.A;
		for(Parity p: stillAlive)
		{
			if(p.compareTo(min) < 0)
			{
				min = p;
			}
		}
		return min;
	}
	
	private Position getNextHUNTTarget()
	{
		Position test;
		for(int col = 0; col < 10; col++)
		{
			for(int row = 0; row < 10; row++)
			{
				test = new Position(row, col);
				if(getGrid().empty(test) && checkParity(test))
				{
					return test;
				}
			}
		}
		return super.shoot();
	}
	
	private Position getNextTARGETTarget()
	{
		Position target = null;
		if(stillAlive.contains(Parity.D) && targetStackD.size() > 0)
		{
			target = targetStackD.pop();
		}
		else if(stillAlive.contains(Parity.S) && targetStackS.size() > 0)
		{
			target = targetStackS.pop();
		}
		else if(stillAlive.contains(Parity.C) && targetStackC.size() > 0)
		{
			target = targetStackC.pop();
		}
		else if(stillAlive.contains(Parity.B) && targetStackB.size() > 0)
		{
			target = targetStackB.pop();
		}
		else if(stillAlive.contains(Parity.A) && targetStackA.size() > 0)
		{
			target = targetStackA.pop();
		}
		System.out.println(target);
		return target;
	}
	
	
	private boolean checkParity(Position pos)
	{
		int row = pos.rowIndex();
		int col = pos.columnIndex();
		boolean rowOK = ((row + 1) % parity.parity) == 0;
		boolean colOK = ((col + 1) % parity.parity) == 0;
		boolean par = (rowOK || colOK) && !(rowOK && colOK);
		int horizontal = checkLeft(pos) + checkRight(pos);
		int vertical = checkAbove(pos) + checkBelow(pos);
		return ((horizontal >= (parity.parity - 1)) || (vertical >= (parity.parity - 1))) && par;
		
	}
	
	private int checkAbove(Position pos)
	{
		int col = pos.columnIndex();
		int row = pos.rowIndex();
		int i  = 0;
		boolean flag = true;
		while(flag)
		{
			try
			{
				flag = getGrid().empty(new Position(row - i, col));
			}
			catch (Exception e)
			{
				flag = false;
			}
			i++;
		}
		return i;
	}
	
	private int checkBelow(Position pos)
	{
		int col = pos.columnIndex();
		int row = pos.rowIndex();
		int i  = 0;
		boolean flag = true;
		while(flag)
		{
			try
			{
				flag = getGrid().empty(new Position(row + i, col));
			}
			catch (Exception e)
			{
				flag = false;
			}
			i++;
		}
		return i;
	}
	
	private int checkLeft(Position pos)
	{
		int col = pos.columnIndex();
		int row = pos.rowIndex();
		int i  = 0;
		boolean flag = true;
		while(flag)
		{
			try
			{
				flag = getGrid().empty(new Position(row, col - i));
			}
			catch (Exception e)
			{
				flag = false;
			}
			i++;
		}
		return i;
	}
	
	private int checkRight(Position pos)
	{
		int col = pos.columnIndex();
		int row = pos.rowIndex();
		int i  = 0;
		boolean flag = true;
		while(flag)
		{
			try
			{
				flag = getGrid().empty(new Position(row, col + i));
			}
			catch (Exception e)
			{
				flag = false;
			}
			i++;
		}
		return i;
	}
	
	private char straggler()
	{
		BattleshipGrid grid = getGrid();
		for(int i = 0; i < 10; i++)
		{
			for(int j = 0; j < 10; j++)
			{
				char abrv = grid.boatInitial(new Position(i, j));
				for(Parity p: stillAlive)
				{
					if(p.name == abrv)
					{
						return abrv;
					}
				}
			}
		}
		return '\u0000';
	}
}
