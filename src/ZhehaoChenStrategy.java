import java.lang.reflect.*;
import java.util.*;

public class ZhehaoChenStrategy extends ComputerBattleshipPlayer
{	
	public static void main(String[] args)
	{
		ZhehaoChenStrategy strategy = new ZhehaoChenStrategy();
		TargetStack<Position> stack = strategy.new TargetStack();
		stack.push(new Position(1, 1));
		stack.push(new Position(2, 1));
		for(Object p: stack)
		{
			System.out.println(p);
		}
		stack.removePosition(new Position(1, 1));
		for(Object p: stack)
		{
			System.out.println(p);
		}
	}
	
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
	private HashSet<Parity> stillAlive;
	private TargetStack<Position> targetStack;
	
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
		targetStack = new TargetStack();
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
			System.out.println("HUNT");
			target = super.shoot();
			System.out.println(target);
			return target;
		}
		else if(status == Status.TARGET)
		{
			System.out.println("TARGET");
			target = getNextTARGETTarget();
			return target;
		}
		return null;
	}
	
	@Override
	public void updatePlayer(Position pos, boolean hit, char initial, String boatName, boolean sunk, boolean gameOver, boolean tooManyTurns, int turns)
	{
		super.updatePlayer(pos, hit, initial, boatName, sunk, gameOver, tooManyTurns, turns);
		parity = smallestParity();
		if(hit)
		{
			status = Status.TARGET;
		}
		else
		{
			status = Status.HUNT;
		}
		manageStack(pos, hit, sunk, initial);
	}
	

	
	private void manageStack(Position pos, boolean hit, boolean sunk, char initial)
	{
		int col = pos.columnIndex();
		int row = pos.rowIndex();
		Position east = null;
		Position west = null;
		Position north = null;
		Position south = null;
		
		if(!hit)
		{
			return;
		}
		
		if(sunk)
		{
			targetStack = new TargetStack();
		}
		
		
		if(col != 9)
		{
			east = new Position(col + 1, row);
			targetStack.push(east);
		}
		else if(col != 0)
		{
			west = new Position(col - 1, row);
			targetStack.push(west);
		}
		else
		{
			
		}
		
		if(row != 0)
		{
			north = new Position(col, row - 1);
			targetStack.push(north);
		}
		else if(row != 9)
		{
			south = new Position(col, row + 1);
			targetStack.push(south);
		}
		else
		{
			
		}
		System.out.println(getGrid().boatInitial(pos));
		if(getGrid().boatInitial(east) == initial || getGrid().boatInitial(west) == initial)
		{
			targetStack.removePosition(north);
			targetStack.removePosition(south);
		}
		else if(getGrid().boatInitial(north) == initial || getGrid().boatInitial(south) == initial)
		{
			targetStack.removePosition(east);
			targetStack.removePosition(west);
		}
		else
		{
			
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
				test = new Position(col, row);
				if(checkParity(test) && super.getGrid().empty(test))
				{
					return test;
				}
			}
		}
		return null;
	}
	
	private Position getNextTARGETTarget()
	{
		return (Position) targetStack.pop();
	}
	
	
	private boolean checkParity(Position pos)
	{
		int col = pos.columnIndex();
		int row = pos.rowIndex();
		
		if(!getGrid().empty(pos))
		{
			return false;
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
