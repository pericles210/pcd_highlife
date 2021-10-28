import java.lang.reflect.InvocationTargetException;

public class pcd_thread_game_of_life{

  public static void main(String[] args) throws InterruptedException,
  InvocationTargetException{

    int n = 2048;
    int n_it = 2000;
    int n_threads = 8;

    long startTime = System.currentTimeMillis();

    double start, end;



    int[][] grid = new int[n][n];
    int[][] new_grid = new int[n][n];

    grid_init(grid);

    simulation(n_it, n, n_threads, grid, new_grid);

    long endTime = System.currentTimeMillis();
    System.out.printf("Tempo de execução: %f s. \n", (float)((endTime-startTime)/1000.0));

  }


  public static void grid_init(int[][] grid){
    //GLIDER
    int lin = 1, col = 1;
    grid[lin][col+1] = 1;
    grid[lin+1][col+2] = 1;
    grid[lin+2][col] = 1;
    grid[lin+2][col+1] = 1;
    grid[lin+2][col+2] = 1;
    //R-pentomino
    lin =10; col = 30;
    grid[lin ][col+1] = 1;
    grid[lin ][col+2] = 1;
    grid[lin+1][col] = 1;
    grid[lin+1][col+1] = 1;
    grid[lin+2][col+1] = 1;
  }



  public static void print_grid(int n, int[][] grid){
    int i, j;
    for(i = 0; i < n; i++){
      for(j = 0; j < n; j++){
        System.out.printf("%d ", grid[i][j]);
      }
      System.out.printf("\n");
    }
  }

  public static void simulation(int n_it, int n, int n_threads, int[][] grid, int[][] new_grid) throws InterruptedException,
			InvocationTargetException{
    int[][] temp;
    int i, j, thread, k, n_living_neigh;
    System.out.printf("** Game of Life\n");

    Thread[] v_t = new Thread[n_threads];
    Run_cell_simu[] v_p = new Run_cell_simu[n_threads];

    Count_living_cells[] c_v = new Count_living_cells[n_threads];
    for(k = 0; k < n_it; k++){
      for(i = 0; i < n_threads; i++){
        v_p[i] = new Run_cell_simu(i, n, i, n_threads, grid, new_grid);
        v_t[i] = new Thread(v_p[i]);
        v_t[i].start();
      }
      for(i = 0; i < n_threads; i++){
        v_t[i].join();

      }


      for(i = 0; i < n_threads; i++){
        c_v[i] = new Count_living_cells(n,i,n_threads, grid);
        v_t[i] = new Thread(c_v[i]);
        v_t[i].start();
      }
      int result = 0;
      for(i = 0; i < n_threads; i++){
        v_t[i].join();
        result+=c_v[i].get_result();
      }
      if(k == 0){ System.out.printf("Condição inicial: %d\n", result); }
      else { System.out.printf("Geração %d: %d\n", k , result); }

      // print_grid(n, &grid);
      temp = grid;
      grid = new_grid;
      new_grid = temp;
    }

    for(i = 0; i < n_threads; i++){
      c_v[i] = new Count_living_cells(n,i,n_threads, grid);
      v_t[i] = new Thread(c_v[i]);
      v_t[i].start();
    }
    int result = 0;
    for(i = 0; i < n_threads; i++){
      v_t[i].join();
      result+=c_v[i].get_result();
    }

    System.out.printf("Última geração (%d iterações): %d células vivas\n", n_it, result);

  }

}

class Count_living_cells implements Runnable{
  private int result, n, start, n_threads;
  private int[][] grid;
  public Count_living_cells(int x, int start, int n_threads, int[][] y){
    this.n = x;
    this.grid = y;
    this.start = start;
    this.n_threads = n_threads;
  }
  public void run(){
    int i, j, result = 0;
    for(i = start; i < n; i+=n_threads){
      for(j = 0; j < n; j++){
        result += grid[i][j];
      }
    }
    this.result = result;
  }
  public int get_result(){
    return result;
  }
}

class Run_cell_simu implements Runnable {
  private int n, i, start, n_threads;
  private int[][] grid, new_grid;
  public Run_cell_simu(int i, int x, int start, int n_threads, int[][] y, int[][] z){
    this.i = i;
    n = x;
    grid = y;
    new_grid = z;
    this.start = start;
    this.n_threads = n_threads;
  }

  public static int get_neighbors(int x, int y, int n, int[][] grid){
    int linha_superior, linha_inferior, coluna_esq, coluna_dir;
    if(x == 0){
      linha_superior = n - 1;
      linha_inferior = 1;
    }
    else if(x == n - 1){
      linha_superior = x - 1;
      linha_inferior = 0;
    }
    else{
      linha_superior = x - 1;
      linha_inferior = x + 1;
    }
    if(y == 0){
      coluna_esq = n - 1;
      coluna_dir = y + 1;
    }
    else if(y == n - 1){
      coluna_esq = y - 1;
      coluna_dir = 0;
    }
    else{
      coluna_esq = y - 1;
      coluna_dir = y + 1;
    }
    // contar arredor de (x,y)
    int qnt_vivos = 0;
    qnt_vivos += (grid[linha_superior][coluna_esq] + grid[linha_superior][y] + grid[linha_superior][coluna_dir]);
    qnt_vivos += (grid[linha_inferior][coluna_esq] + grid[linha_inferior][y] + grid[linha_inferior][coluna_dir]);
    qnt_vivos += (grid[x][coluna_esq] + grid[x][coluna_dir]);

    return qnt_vivos;
  }

  public void run(){
    int i, j, n_living_neigh;
    // pcd_thread pcd;
    for(i = start; i < n; i+=n_threads){
      for(j = 0; j < n; j++){
        n_living_neigh = get_neighbors(i, j, n, grid);
        if((n_living_neigh == 2 || n_living_neigh == 3) && grid[i][j] == 1){ // caso 1
          new_grid[i][j] = 1;
        }
        else if((n_living_neigh == 3) && grid[i][j] == 0){ // caso 2
          new_grid[i][j] = 1;
        }
        else{ // caso 3
          new_grid[i][j] = 0;
        }
      }

    }
  }

}
