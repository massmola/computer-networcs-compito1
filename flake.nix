{
  description = "A development shell for a Java project";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      supportedSystems = [ "x86_64-linux" "aarch64-linux" "x86_64-darwin" "aarch64-darwin" ];
      forEachSupportedSystem = f: nixpkgs.lib.genAttrs supportedSystems (system: f system);
    in
    {
      devShells = forEachSupportedSystem (system:
        let
          pkgs = nixpkgs.legacyPackages.${system};
        in
        {
          default = pkgs.mkShell {
            
            buildInputs = [
              # --- This is the updated line ---
              pkgs.jdk21    # Java Development Kit 21 (Current LTS)
              # ------------------------------

              pkgs.maven    # Maven build tool
              pkgs.gradle   # Gradle build tool
            ];

            shellHook = ''
              echo "Welcome to your Java 21 dev shell!"
              export JAVA_HOME="${pkgs.jdk21}"
            '';
          };
        });
    };
}