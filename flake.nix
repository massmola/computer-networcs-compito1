{
  description = "Flake providing a Java dev shell (OpenJDK 17) for the project";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-23.11";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        openjdk = pkgs.openjdk17;
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = [ openjdk ];
          shellHook = ''
            echo "Entered Java dev shell using ${openjdk.pname}"
            # javac and java are on PATH
            echo "javac: $(which javac)"
            echo "java:  $(which java)"
          '';
        };
      }
    );
}
