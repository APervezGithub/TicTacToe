import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class TicTacToe extends PApplet {

/**
 * Class for each square in the board
**/
class BoardSquare
{
  /** What the square is filled with. 0 is empty, -1 is cross, 1 is circle */
  int SquareStatus;
  
  /** Index of square in board */
  int Index;
  
  /** Style variables */
  int BorderSize;
  float BoxPadding;
  int BoxColor;
  int BoxBorderColor;
  int HighlightBoxColor;
  int CrossColor;
  int CircleColor;
  
  /** Square coordinates */
  int x1;
  int x2;
  int y1;
  int y2;
  
  /** Square dimensions */
  int w;
  int h;
  
  /** Set default values */
  BoardSquare(int x1_, int x2_, int y1_, int y2_, int Index_)
  {
    SquareStatus = 0;    
    Index = Index_;
    
    BorderSize = 5;
    BoxPadding = 0.3f;
    BoxColor = color(49, 51, 110);
    BoxBorderColor = color(27);
    HighlightBoxColor = color(95, 67, 115);
    CrossColor = color(234, 210, 108);
    CircleColor = color(224, 104, 92);
    
    x1 = x1_;
    x2 = x2_;
    y1 = y1_;
    y2 = y2_;
    
    w = x2 - x1;
    h = y2 - y1;
  }
  
  /** Draw square background */
  public void DrawBox(int c)
  {
    fill(c);
    stroke(BoxBorderColor);
    strokeWeight(BorderSize);
    rect (x1, y1, w, h);
  }
  
  /** Display square */
  public void Display(int Player, boolean ShouldHighlight)
  {
    switch(Player)
    {
      case -1:
        HighlightBoxColor = CrossColor;
      break;
      case 1:
        HighlightBoxColor = CircleColor = color(224, 104, 92);
      break;
    }
    switch(SquareStatus)
    {
      // Draw empty square, may be highlighted
      case 0:
        if (MouseInBox() && ShouldHighlight)
        {
          DrawBox(HighlightBoxColor);
        }
        else
        {
          DrawBox(BoxColor);
        }
      break;
      // Draw circle square
      case 1:
        DrawBox(BoxColor);
        noFill();
        stroke(CircleColor);
        strokeWeight(BorderSize);
        ellipse (x1 + w / 2, y1 + h / 2, w / 2 * (1 - BoxPadding), h / 2 * (1 - BoxPadding));
      break;
      // Draw cross square
      case -1:
        DrawBox(BoxColor);
        noFill();
        stroke(CrossColor);
        strokeWeight(BorderSize);
        line (x1 + w * BoxPadding, y1 + h * BoxPadding, x2 - w * BoxPadding, y2 - h * BoxPadding);
        line (x1 + w * BoxPadding, y2 - h * BoxPadding, x2 - w * BoxPadding, y1 + h * BoxPadding);
      break;
    }
  }
  
  /** Returns whether the mouse is in the box */
  public boolean MouseInBox()
  {
    return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
  }
  
  /** Returns whether the box can be played in */
  public boolean IsPlaying()
  {
    return MouseInBox() && (SquareStatus == 0);
  }
  
  /** Plays in the box */
  public void Play(int Player)
  {
    SquareStatus = Player;
  }
}

/**
 * Class for the game board
**/
class Board
{
  /** Array of squares in board */
  ArrayList<BoardSquare> Squares;
  
  /** Coordinates of board */
  int x;
  int y;
  
  /** How many squares are filled */
  int FilledSquares;
  /** Number of squares in line of board and total count of squares */
  int BoardNum;
  int BoardCount;
  /** Size of each square */
  int SquareSize;
  
  /** Totals of each row, column, and diagonal. Used to calculate game end */
  int[] RowTotals;
  int[] ColTotals;
  int LDiagTotal;
  int RDiagTotal;
  
  /** Which player's turn it is */ 
  int PlayerTurn;
  /** Who has won. 0 is no one, 1 is circle, 2 is cross, 3 is tie */
  int Winner;
  
  /** Style variables */
  int WinnerColor;
  int RestartColor;
  int xWinnerText;
  int yWinnerText;
  int yRestartText;
  int WinnerTextSize;
  int RestartTextSize;
  
  /** Set default values */
  Board(int x_, int y_, int BoardNum_, int SquareSize_)
  {
    Initialize(x_, y_, BoardNum_, SquareSize_);
  }
  
  /** Set default values */
  public void Initialize(int x_, int y_, int BoardNum_, int SquareSize_)
  {
    x = x_;
    y = y_;
    
    BoardNum = BoardNum_;
    BoardCount = BoardNum * BoardNum;
    SquareSize = SquareSize_;
    FilledSquares = 0;
    
    Squares = new ArrayList<BoardSquare>();
    
    RowTotals = new int[BoardCount];
    ColTotals = new int[BoardCount];
    
    // Create board squares
    for (int i = 0; i < BoardNum; ++i)
    {
      for (int j = 0; j < BoardNum; ++j)
      {
        Squares.add(new BoardSquare(x + i * SquareSize, x + (i + 1) * SquareSize, y + j * SquareSize, y + (j + 1) * SquareSize, i + j * 3));
      }
      RowTotals[i] = 0;
      ColTotals[i] = 0;
    }
    
    LDiagTotal = 0;
    RDiagTotal = 0;
    
    PlayerTurn = 1;
    Winner = 0;
    
    WinnerColor = color(95, 67, 115);
    RestartColor = color(95, 67, 115);
    
    xWinnerText = 300;
    yWinnerText = 10;
    yRestartText = 50;
    WinnerTextSize = 32;
    RestartTextSize = 16;
  }
  
  /** Display board */
  public void Display()
  {
    for (int i = 0; i < Squares.size(); ++i)
    {
      BoardSquare Square = Squares.get(i);
      Square.Display(PlayerTurn, Winner == 0);
    }
    ShowResult();
  }
  
  /** Make a play in selected tile */
  public void MakePlay()
  {
    // Only play is to restart if board is done
    if (Winner != 0)
    {
      Initialize(x, y, BoardNum, SquareSize);
      return;
    }
    
    int i;
    boolean FoundPlay = false;
    for (i = 0; i < Squares.size(); ++i)
    {
      BoardSquare Square = Squares.get(i);
      // Finds which square is playable and includes mouse coordinates
      if (Square.IsPlaying())
      {
        Square.Play(PlayerTurn);
        FoundPlay = true;
        break;
      }
    }
    
    if (FoundPlay)
    {
      FilledSquares += 1;
      IncrementTotals(PlayerTurn, i);
      PlayerTurn *= -1;
    }
  }
  
  /** Increment board totals */
  public void IncrementTotals(int Player, int Index)
  {
    int xIndex = Index % BoardNum;
    int yIndex = Index / BoardNum;
    
    RowTotals[xIndex] += Player;
    ColTotals[yIndex] += Player;
    
    // TL to BR diagonal will have equivalent x and y
    if (xIndex == yIndex)
    {
      LDiagTotal += Player;
    }
    // TR to BL diagonal will have x and y sum to board size adjusted for 0 index
    if (xIndex + yIndex == BoardNum - 1)
    {
      RDiagTotal += Player;
    }
    
    CheckEnd(Player, xIndex, yIndex);
  }
  
  /** Check if game has ended */
  public void CheckEnd(int Player, int xIndex, int yIndex)
  {
    int DesiredTotal = Player * 3;
    
    // Check if any totals contain only one player
    if (RowTotals[xIndex] == DesiredTotal ||
        ColTotals[yIndex] == DesiredTotal ||
        LDiagTotal == DesiredTotal ||
        RDiagTotal == DesiredTotal)
    {
      Winner = Player;
    }
    else if (FilledSquares == pow(BoardNum, 2))
    {
      Winner = 3;
    }
  }
  
  /** Show results of game, if any */
  public void ShowResult()
  {
    textAlign(CENTER, TOP);
    textSize(WinnerTextSize);
    if (Winner == 0)
    {
      return;
    }
    else if (Winner == 1)
    {
      WinnerColor = Squares.get(0).CircleColor;
      fill(WinnerColor);
      text("Player O Wins", xWinnerText, yWinnerText);
    }
    else if (Winner == -1)
    {
      WinnerColor = Squares.get(0).CrossColor;
       fill(WinnerColor);
       text("Player X Wins", xWinnerText, yWinnerText);
    }
    if (Winner == 3)
    {
      fill(WinnerColor);
      text("Both Players Lose", xWinnerText, yWinnerText);
    }
    fill(RestartColor);
    textSize(RestartTextSize);
    text("Click Anywhere to Restart", xWinnerText, yRestartText);
  }
}

Board GameBoard;

public void setup()
{
  frameRate(60);
  
  GameBoard = new Board(75, 75, 3, 150);
}

public void draw()
{
  background(27);
  GameBoard.Display();
}

public void mouseReleased() {
  GameBoard.MakePlay();
}
  public void settings() {  size(600, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "TicTacToe" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
