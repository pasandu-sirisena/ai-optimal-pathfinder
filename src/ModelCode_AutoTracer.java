import java.util.concurrent.TimeUnit;
import java.util.*;

public class ModelCode_AutoTracer {
    
    public static void main(String args[]) throws Exception
    {
        int complexity = setDifficultyLevel();
        
        TileMap myMap = new TileMap(complexity); 
        Tracer myTracer = new Tracer(1, 1, myMap, complexity);
        boolean exitFlag = false;   
        
        // Program Loop
        do
        {
            // CLEAR SCREEN : This is the ANSI way in Java, but still need validations on different OSes
            System.out.print("\033[H\033[2J");      // need to test on Mac and Linux.  
            System.out.flush();                       // Watch out for M silicon platform.
            
            // RUN LOGIC
            // Tracer makes path finding at Start (S) : Lab 3 / Every Intersection (I) : Lab 4 and 5
            // Tracer follows path at every loop iter.
            // Game Loop Ends when 
            //  a) Tracer arrives at D - draw, then END.
            //  b) Tracer sees risk score > 100, leading arrest - draw, then END.
            // Record 1) Tiles Travelled, 2) Total Time to Reach D, 3) Risk Score to Reach D

            myTracer.makeAMove();
            if(myTracer.isCompleted()) exitFlag = true;

            // DRAW SCREEN
            Tile[][] mapRef = myMap.getMapRef();
            LinkedList<Tile> path = myTracer.getFullPath();
            if(path == null)
            {
                System.out.println("The path finding algorithm has not been implemented for this map type.");
                System.out.println("Game Shutting Down!!");
                break;
            }

            // need an overall Draw() routine.    
            for(int i = 0; i < TileMap.BOARDSIZEY; i++)
            {
                for(int j = 0; j < TileMap.BOARDSIZEX; j++)
                {
                    
                    if(myTracer.getY() == i && myTracer.getX() == j)    // Top: Tracer
                    {
                        System.out.printf("%c", Tracer.SYMBOL);
                    }
                    else if(path.contains(mapRef[i][j]) && mapRef[i][j].getTileType() == ' ')   // Middle: Path
                    {
                        System.out.printf("%c", '.');
                    }
                    else     // Bottom: Map
                    {
                        System.out.printf("%c", mapRef[i][j].getTileType());
                    }
                }
                System.out.printf("\n");
            }       

            System.out.println("Optimal Path: ");
            myTracer.printPath();
            
            // LOOP DELAY
            TimeUnit.MICROSECONDS.sleep(100000);  // delay for 0.5 seconds

        } while(!exitFlag);
    }

    private static int setDifficultyLevel()
    {
        int input;
        Scanner myInputScanner = new Scanner(System.in);

        System.out.println("Select Test Setup:");
        System.out.println("0 - Plain Map, RIGHT/DOWN Direction Only (DAG)");
        System.out.println("1 - Map with Random Penalties (DAG): Topological Sort Shortest Path Algorithm");
        System.out.println("2 - Map with Random Penalties (DAG): Dijkastra Shortest Path Algorithm");
        System.out.println("3 - Map with Random Penalties and Rewards (DAG): Bellman-Ford Shortest Path Algorithm");

        input = myInputScanner.nextInt();
        myInputScanner.close();

        if(input < 0) input = 0;
        else if(input > 3) input = 3;       
        
        return input;
    }

}


