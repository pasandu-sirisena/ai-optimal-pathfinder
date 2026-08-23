// Lab 4 Update:

// Smaller Map Size for faster verification (40x15)
// Score Penality generation chance = 2.5% per tile
// Score Reward generation chance = 1.5% per tile

import java.util.Random;

public class TileMap {
    
    public static final int BOARDSIZEX = 40;
    public static final int BOARDSIZEY = 15;

    public static final int RANDRANGE = 2000;
    public static final int REWARDDOWNCHANGE = 250;  // %2.5
    public static final int REWARDUPCHANGE = 150;    // %1.5

    private Tile[][] map = new Tile[BOARDSIZEY][BOARDSIZEX];    

    public TileMap(int complexity)
    {
        Tile[] intersectionList;
        int intersectionCount;
        
        // Step 1: Populate the Entire Board with S, D, and Walls
        for(int i = 0; i < BOARDSIZEY; i++)
            {   
                for(int j = 0; j < BOARDSIZEX; j++)
                {
                    if(i == 1 && j == 1)
                    {
                        map[i][j] = new Tile(j, i, 'S', 0);
                    }
                    else if(i == BOARDSIZEY - 2 && j == BOARDSIZEX - 2)
                    {
                        map[i][j] = new Tile(j, i, 'D', 0);
                    }
                    else
                    {
                        map[i][j] = new Tile(j, i, '#', 0);
                    }
                }
            }

        // Step 2: Seed the Board with Initial Intersections based on the required Complexity
        intersectionCount = 6;    
        intersectionList = generateIntersections(intersectionCount);

        // Step 3: Generate Paths
        completePathGen(intersectionList, intersectionCount);
        randomizePath(complexity);  // then randomly generate path properties

        // Pre-print the map for confirmation
        for(int i = 0; i < TileMap.BOARDSIZEY; i++)
        {
            for(int j = 0; j < TileMap.BOARDSIZEX; j++)
            {
                System.out.printf("%c", map[i][j].getTileType());
            }
            System.out.printf("\n");
        } 
    }

    private Tile[] generateIntersections(int intCount)
    {
        int count = 0;
        int tempX, tempY;        
        Tile[] tempTile = new Tile[intCount];        
        boolean skipFlag;

        Random rand = new Random();

        while(count < intCount)
        {
            tempX = rand.nextInt(BOARDSIZEX / 3 - 2) * 3 + 3;   // make sure intersections never neighbour each other
            tempY = rand.nextInt(BOARDSIZEY / 3 - 2) * 3 + 3;   // make sure intersections never neighbour each other
            skipFlag = false;

            for(int i = 0; i < count; i++)
            {
                if( (tempTile[i].getX() == tempX && tempTile[i].getY() == tempY) || 
                    (tempTile[i].getX() == tempX && tempTile[i].getY() == tempY + 1) || 
                    (tempTile[i].getX() == tempX && tempTile[i].getY() == tempY - 1) || 
                    (tempTile[i].getX() == tempX + 1 && tempTile[i].getY() == tempY) || 
                    (tempTile[i].getX() == tempX + 1 && tempTile[i].getY() == tempY + 1) || 
                    (tempTile[i].getX() == tempX + 1 && tempTile[i].getY() == tempY - 1) || 
                    (tempTile[i].getX() == tempX - 1 && tempTile[i].getY() == tempY) || 
                    (tempTile[i].getX() == tempX - 1 && tempTile[i].getY() == tempY + 1) || 
                    (tempTile[i].getX() == tempX - 1 && tempTile[i].getY() == tempY - 1) )
                {
                    skipFlag = true;
                    break;
                }
            }
            if(skipFlag) continue;  // duplicated, try another candidate

            tempTile[count] = new Tile(tempX, tempY, 'I', 0);
            map[tempY][tempX] = new Tile(tempX, tempY, 'I', 0);
            count++;
        }

        return tempTile;
    }

    private void completePathGen(Tile[] IntList, int IntCount)
    {
        int currX, currY;
        
        // First for S
        currX = 1;
        currY = 1;
        for(int i = currX + 1; i < BOARDSIZEX - 1; i++)
        {
            map[currY][i] = new Tile(i, currY, ' ', 0);
        }
        for(int i = currY + 1; i < BOARDSIZEY - 1; i++)
        {
            map[i][currX] = new Tile(currX, i, ' ', 0);
        }

        // Then for D
        currX = BOARDSIZEX - 2;
        currY = BOARDSIZEY - 2;
        for(int i = currX - 1; i > 1; i--)
        {
            map[currY][i] = new Tile(i, currY, ' ', 0);
        }
        for(int i = currY - 1; i > 1; i--)
        {
            map[i][currX] = new Tile(currX, i, ' ', 0);
        }
        
        // Next, for all the intersections 
        boolean walled = false;        

        for(int i = 0; i < IntCount; i++)
        {            
            // Extend Paths from Every I
            currX = IntList[i].getX();
            currY = IntList[i].getY();
            
            // LEFT
            if(map[currY][currX - 1].getTileType() != ' ')
            {
                walled = true;
                for(int j = currX - 1; j > 0; j--) // stop at wall
                {
                    //         - Whenever crossed with another I, stop
                    if(map[currY][j].getTileType() == 'I')    
                    {
                        walled = false;                        
                        break;
                    }
                    //         - Whenever crossed with another path, mark additional I, do not extend
                    else if(map[currY][j].getTileType() == ' ')
                    {
                        map[currY][j] = new Tile(j, currY, 'I', 0);
                        walled = false;                        
                        break;
                    }
                    map[currY][j] = new Tile(j, currY, ' ', 0);
                }

                if(walled)
                {
                    for(int j = 1; j <= currX - 1; j++)
                        map[currY][j] = new Tile(j, currY, '#', 0);
                }
            }


            // RIGHT
            if(map[currY][currX + 1].getTileType() != ' ')
            {
                walled = true;
                for(int j = currX + 1; j < BOARDSIZEX - 1; j++) // stop at wall
                {
                    //         - Whenever crossed with another I, stop
                    if(map[currY][j].getTileType() == 'I')    
                    {
                        walled = false; 
                        break;
                    }
                    //         - Whenever crossed with another path, mark additional I, do not extend
                    else if(map[currY][j].getTileType() == ' ')
                    {
                        map[currY][j] = new Tile(j, currY, 'I', 0);
                        walled = false; 
                        break;
                    }
                    map[currY][j] = new Tile(j, currY, ' ', 0);
                }

                if(walled)
                {
                    for(int j = BOARDSIZEX - 1; j >= currX + 1; j--)
                        map[currY][j] = new Tile(j, currY, '#', 0);
                }
            }


            // UP
            // sanity check - if already pathed, do not regen
            if(map[currY - 1][currX].getTileType() != ' ')
            {
                walled = true;
                for(int j = currY - 1; j > 0; j--) // stop at wall
                {
                    //         - Whenever crossed with another I, stop
                    if(map[j][currX].getTileType() == 'I')    
                    {
                        walled = false;
                        break;
                    }
                    //         - Whenever crossed with another path, mark additional I, do not extend
                    else if(map[j][currX].getTileType() == ' ')
                    {
                        map[j][currX] = new Tile(currX, j, 'I', 0);
                        walled = false;
                        break;
                    }
                    map[j][currX] = new Tile(currX, j, ' ', 0);
                }

                if(walled)
                {
                    for(int j = 1; j <= currY - 1; j++)
                        map[j][currX] = new Tile(currX, j, '#', 0);
                }
            }

            // DOWN
            // sanity check - if already pathed, do not regen
            if(map[currY + 1][currX].getTileType() != ' ')
            {
                walled = true;
                for(int j = currY + 1; j < BOARDSIZEY - 1; j++) // stop at wall
                {
                    //         - Whenever crossed with another I, stop
                    if(map[j][currX].getTileType() == 'I')    
                    {
                        walled = false;
                        break;
                    }
                    //         - Whenever crossed with another path, mark additional I, do not extend
                    else if(map[j][currX].getTileType() == ' ')
                    {
                        map[j][currX] = new Tile(currX, j, 'I', 0);
                        walled = false;
                        break;
                    }
                    map[j][currX] = new Tile(currX, j, ' ', 0);
                }

                if(walled)
                {
                    for(int j = BOARDSIZEY - 1; j >= currY + 1; j--)
                        map[j][currX] = new Tile(currX, j, '#', 0);
                }
            }
        }

        // Finally, retrack S and D extensions to the closest Intersection
        // First for S
        currX = 1;
        currY = 1;
        for(int i = BOARDSIZEX - 2; i > currX; i--)
        {
            if(map[currY][i].getTileType() == 'I') 
                break;

            map[currY][i] = new Tile(i, currY, '#', 0);
        }
        for(int i = BOARDSIZEY - 2; i > currY; i--)
        {
            if(map[i][currX].getTileType() == 'I') 
                break;
            map[i][currX] = new Tile(currX, i, '#', 0);
        }

        // Then for D
        currX = BOARDSIZEX - 2;
        currY = BOARDSIZEY - 2;
        for(int i = 2; i < BOARDSIZEX - 2; i++)
        {
            if(map[currY][i].getTileType() == 'I') 
                break;
            map[currY][i] = new Tile(i, currY, '#', 0);
        }
        for(int i = 2; i < BOARDSIZEY - 2; i++)
        {
            if(map[i][currX].getTileType() == 'I') 
                break;
            map[i][currX] = new Tile(currX, i, '#', 0);
        }

    }

    public void randomizePath(int complexity)
    {
        // complexity level:
        // 0 - no penalty or police
        // 1 - only penalty, DAG (Topological Sort Algorithm)
        // 2 - only penalty, DAG (Dijkstra Algorithm)
        // 3 - both penalty and reward, DAG (Bellmen-Ford Algorithm)
        if(complexity == 0) return;

        Random rand = new Random();
        int randNum;

        for(int i = 0; i < BOARDSIZEY; i++)
        {
            for(int j = 0; j < BOARDSIZEX; j++)
            {
                if(map[i][j].getTileType() == ' ')  // only modify the path tile property
                {
                    randNum = rand.nextInt(RANDRANGE);

                    if(randNum < REWARDDOWNCHANGE && complexity > 0) 
                        map[i][j] = new Tile(j, i, 'x', 5);
                    else if(RANDRANGE - randNum < REWARDUPCHANGE && complexity > 2)                    
                        map[i][j] = new Tile(j, i, '$', -2);
                }
            }
        }
    }
    
    public void printMap()
    {
        for(int i = 0; i < BOARDSIZEY; i++)
        {
            for(int j = 0; j < BOARDSIZEX; j++)
            {
                System.out.printf("%c", map[i][j].getTileType());
            }
            System.out.printf("\n");
        }
    }

    public Tile[][] getMapRef() { return map; }

    public Tile getStartTile() { return map[1][1]; }
    
    public Tile getEndTile() { return map[BOARDSIZEY - 2][BOARDSIZEX - 2]; }
}
