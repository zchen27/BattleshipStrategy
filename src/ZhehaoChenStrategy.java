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
			target = super.shoot();
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
			status = Status.HUNT;
			currentTarget = straggler();
		}
		manageStack(pos, hit, sunk, currentTarget);
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

		
		if(!hit)
		{
			return;
		}
		
		if(sunk)
		{
			switch(initial)
			{
				case 'A':
					targetStackA = null;
				case 'B':
					targetStackB = null;
				case 'C':
					targetStackC = null;
				case 'S':
					targetStackS = null;
				case 'D':
					targetStackD = null;
			}
		}
		

		
		switch(initial)
		{
			case 'A':
				
		}
		
	}
	
	private void stackA(Position pos)
	{
		int col = pos.columnIndex();
		int row = pos.rowIndex();
		Position east = null;
		Position west = null;
		Position north = null;
		Position south = null;
		
		if(col >= 1)
		{
			west = new Position(row, col - 1);
		}
		
		if(col <= 8)
		{
			east = new Position(row, col + 1);
		}
		
		if(row >= 1)
		{
			north = new Position(row - 1, col);
		}
		
		if(row <= 8)
		{
			south = new Position(row + 1, col);
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
				if(checkParity(test))
				{
					return test;
				}
			}
		}
		return null;
	}
	
	private Position getNextTARGETTarget()
	{
		Position target = super.shoot();
		
		
		for(int i = 0; i < targetStack.size(); i++)
		{
			System.out.print(targetStack.get(i) + " ");
		}
		System.out.println("");
		
		try
		{
			target = targetStack.pop();
		}
		catch(NullPointerException e)
		{
			System.out.println("EXCEPTION");
			e.printStackTrace();
		}
		
		if(!getGrid().empty(target))
		{
			System.out.println("POP BECAUSE OCCUPIED");
			target = targetStack.pop();
		}
		return target;
	}
	
	
	private boolean checkParity(Position pos)
	{
		int col = pos.columnIndex();
		int row = pos.rowIndex();
		boolean colYes = (col + 1) % parity.parity == 0;
		boolean rowYes = (row + 1) % parity.parity == 0;
		
		if(!getGrid().empty(pos))
		{
			return false;
		}
		
		if(!(colYes ^ rowYes))
		{
			return false;
		}
		
		
		for(int i = 1; i < parity.parity; i++)
		{
			
		}
		return true;
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
