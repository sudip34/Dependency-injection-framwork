package com.ssaha;

@Singleton
public class MyApp {
    @Inject
    private DBService dbService;
    public boolean isDBServiceSet(){
        return dbService != null;
    }
}
