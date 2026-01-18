import Link from "next/link";
import ProjectsPage from "@/modules/proyectos/pages/ProjectsPage";
import { Button } from "@/components/ui/button";

export default function Home() {
  return (
    <div className="space-y-6">
      <div className="flex justify-end">
        <Link href="/landing">
          <Button variant="outline">Ver Landing</Button>
        </Link>
      </div>
      <ProjectsPage />
    </div>
  );
}
