#include <stdio.h>
#include <stdlib.h>
#include <omp.h>

double tempo_seq=0, start_seq;

int **create_grid(int n){
  int **m;
  int i, j;
  m = (int**) malloc(n * sizeof(int*));
  for(i = 0; i < n; i++){
    m[i] = (int*) calloc(n, sizeof(int));
  }
  return m;
}

int get_neighbors(int x, int y, int n, int ***m){
  int linha_superior, linha_inferior, coluna_esq, coluna_dir;
  int **grid = *m;
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

void grid_init(int*** m){
  int **grid = *m;
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

int count_living_cells(int n, int*** m){
  int **grid = *m;
  int i, j, result = 0;
  #pragma omp parallel for private(i,j) reduction(+ : result)
  for(i = 0; i < n; i++){
    for(j = 0; j < n; j++){
      result += grid[i][j];
    }
  }
  return result;
}

void print_grid(int n, int*** m){
  int **grid = *m;
  int i, j;
  for(i = 0; i < n; i++){
    for(j = 0; j < n; j++){
      printf("%d ", grid[i][j]);
    }
    printf("\n");
  }
}

void simulation(int n_it, int n, int ***m1, int ***m2){
  start_seq = omp_get_wtime();
  int **grid = *m1;
  int **new_grid = *m2;
  int **temp;
  int i, j, k, n_living_neigh;
  printf("** Game of Life\n");

  for(k = 0; k < n_it; k++){
    #pragma omp parallel for private (i,j,n_living_neigh) shared(n,grid,new_grid)
      for(i = 0; i < n; i++){
        for(j = 0; j < n; j++){
          n_living_neigh = get_neighbors(i, j, n, &grid);
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
    if(k == 0){ printf("Condição inicial: %d\n", count_living_cells(n, &grid)); }
    else { printf("Geração %d: %d\n", k , count_living_cells(n, &grid)); }

    start_seq = omp_get_wtime();
    // print_grid(n, &grid);
    temp = grid;
    grid = new_grid;
    new_grid = temp;
    tempo_seq += omp_get_wtime() - start_seq;
  }
  printf("Última geração (%d iterações): %d células vivas\n", n_it, count_living_cells(n, &grid));

}

int main(int argc, char const *argv[]) {
  start_seq = omp_get_wtime();
  omp_set_num_threads(1);
  double start, end;
  start = omp_get_wtime();

  int n = 2048;

  int **grid = create_grid(n);
  int **new_grid = create_grid(n);

  grid_init(&grid);

  int n_it = 2000;

  tempo_seq += omp_get_wtime() - start_seq;
  simulation(n_it, n, &grid, &new_grid);

  start_seq = omp_get_wtime();

  end = omp_get_wtime();

  printf("Tempo de execução: %f s. \n", end-start);


  tempo_seq += omp_get_wtime() - start_seq;
  // printf("Tempo de execução sequencial: %f s. \n", tempo_seq + (n_it * 0.000001 * 8.8) + (n_it * 0.000001 * 9.4));


  return 0;
}
