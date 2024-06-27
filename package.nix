{ pkgs, jdk }:
let
  buildMavenPackage = (pkgs.maven.override { inherit jdk; }).buildMavenPackage;
in
buildMavenPackage rec {
  pname = "missilewars";
  src = ./.;
  mvnHash = "sha256-Ihti60D5ag+D/yvmvp+CWkxHkSphfENiz28K01yWkPA=";
  version = "4.7.1";
  installPhase = ''
    mkdir -p $out/plugins
    cp ./missilewars-plugin/target/*.jar $out/plugins/
  '';
  meta = {
    description = "MissileWars is a famous, fun and fast minigame spigot-plugin for Minecraft";
    homepage = "https://github.com/RedstoneFuture/missilewars";
    license = pkgs.lib.licenses.gpl3;
  };
}
